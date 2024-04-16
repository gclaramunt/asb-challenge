package gclaramunt.asbchallenge.prservice

import cats.effect.{IO, IOApp}
import fs2.Chunk
import gclaramunt.asbchallenge.{Config, PRData}
import gclaramunt.asbchallenge.Config.GitHubConfig
import gclaramunt.asbchallenge.storage.PRStore.insert
import gclaramunt.asbchallenge.storage.session
import github4s.{GHResponse, GithubClient}
import github4s.domain.{PRFilterClosed, PRFilterOpen, Pagination, PullRequest}
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import skunk.Session
import skunk.data.Completion

class PRFetchSvc(httpClient: Client[IO]) {

  lazy val GitHubConfig(token, repositories, pageSize) = Config.asbConfig.github

  private def getPRs(
      user: String,
      repo: String,
      page: Int
  ): IO[List[PullRequest]] = {

    val filter = List(PRFilterOpen, PRFilterClosed)

    val pagination = if (page < 0) None else Some(Pagination(page, pageSize))
    GithubClient[IO](
      httpClient,
      Some(token)
    ).pullRequests
      .listPullRequests(user, repo, filters = filter, pagination = pagination)
      .flatMap {
        case GHResponse(Right(prList), _, _) => IO.pure(prList)
        case GHResponse(Left(error), _, _)   => IO.raiseError(error)
      }

  }

  def prStream(): fs2.Stream[IO, PRData] =
    fs2.Stream.emits(repositories).flatMap { case (user, repo) =>
      fs2.Stream.unfoldChunkEval((0, false)) { case (page, stop) =>
        if (stop) {
          IO.pure(None)
        } else
          for {
            ghPrs <- getPRs(user, repo, page)
          } yield {
            val prs = ghPrs.map { PRData.from(user, repo, _) }.toIndexedSeq
            print(prs)
            val (nextPage, stopNext) =
              if (ghPrs.size < pageSize) (0, true) else (page + 1, false)
            Some((Chunk.from(prs), (nextPage, stopNext)))
          }
      }

    }

  def fetchAndStorePR(s: Session[IO]): fs2.Stream[IO, Completion] = {
    fs2.Stream.eval(s.prepare(insert)).flatMap { pc =>
      prStream()
        .through(pc.pipe)
    }

  }

}

object PRFetchApp extends IOApp.Simple {
  val run = {
    import natchez.Trace.Implicits.noop

    (for {
      pooled <- session[IO]
      s <- pooled
      client <- EmberClientBuilder.default[IO].build
    } yield {
      (s, client)
    }).use { case (s, client) =>
      new PRFetchSvc(client)
        .fetchAndStorePR(s)
        .compile
        .drain
        .onError(IO.println)
    }

  }
}
