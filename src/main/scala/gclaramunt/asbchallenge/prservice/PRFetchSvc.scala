package gclaramunt.asbchallenge.prservice

import cats.effect.{IO, IOApp}
import fs2.Chunk
import gclaramunt.asbchallenge.Config.GitHubConfig
import gclaramunt.asbchallenge.storage.PRStore.{insertPR, insertPRCommit}
import gclaramunt.asbchallenge.storage.session
import gclaramunt.asbchallenge.{Config, PRData, PRUserCommit}
import github4s.domain.{PRFilterClosed, PRFilterOpen, Pagination, PullRequest}
import github4s.interpreters.StaticAccessToken
import github4s.modules.GithubAPIsV3
import github4s.{GHResponse, GithubClient, GithubConfig}
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import skunk.Session
import skunk.data.Completion

class PRFetchSvc(httpClient: Client[IO]) {

  lazy val GitHubConfig(token, repositories, pageSize) = Config.asbConfig.github

  def getPRCommits(
      user: String,
      repo: String,
      pullId: Int,
      page: Int
  ): IO[List[PRUserCommit]] = {

    import github4s.algebras.AccessHeader

    val client = new GithubAPIsV3[IO](
      httpClient,
      GithubConfig.default,
      AccessHeader.from(new StaticAccessToken(Some(token)))
    ).httpClient

    val pagination = if (page < 0) None else Some(Pagination(page, pageSize))

    client
      .get[List[PRUserCommit]](
        s"repos/$user/$repo/pulls/$pullId/commits",
        pagination = pagination,
        headers = Map("X-GitHub-Api-Version" -> "2022-11-28")
      )
      .flatMap {
        case GHResponse(Right(prList), _, _) =>
          IO.pure(prList.map(pr => PRUserCommit(pr.sha, pr.login, pullId)))
        case GHResponse(Left(error), _, _) => IO.raiseError(error)
      }
  }

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
      .listPullRequests(
        user,
        repo,
        filters = filter,
        pagination = pagination,
        headers = Map("X-GitHub-Api-Version" -> "2022-11-28")
      )
      .flatMap {
        case GHResponse(Right(prList), _, _) => IO.pure(prList)
        case GHResponse(Left(error), _, _)   => IO.raiseError(error)
      }

  }

  def ghPagedSource[I, O](sourceFn: Int => IO[List[O]]): fs2.Stream[IO, O] = {
    fs2.Stream.unfoldChunkEval((0, false)) { case (page, stop) =>
      if (stop) {
        IO.pure(None) // this doesn't infer properly with a generic F[_]
      } else
        for {
          elems <- sourceFn(page)
        } yield {
          val (nextPage, stopNext) =
            if (elems.size < pageSize) (0, true) else (page + 1, false)
          Some((Chunk.from(elems), (nextPage, stopNext)))
        }
    }
  }

  def prStream(): fs2.Stream[IO, PRData] =
    fs2.Stream.emits(repositories).flatMap { case (user, repo) =>
      ghPagedSource { page =>
        getPRs(user, repo, page).map(_.map { PRData.from(user, repo, _) })
      }
    }

  def prCommitsStream(
      prs: fs2.Stream[IO, PRData]
  ): fs2.Stream[IO, PRUserCommit] =
    prs.flatMap { pr =>
      ghPagedSource { page =>
        getPRCommits(pr.projectUser, pr.projectRepo, pr.id, page)
      }
    }

  def storePR(
      s: Session[IO]
  )(prs: fs2.Stream[IO, PRData]): fs2.Stream[IO, Completion] = {
    fs2.Stream.eval(s.prepare(insertPR)).flatMap { pc =>
      prs.through(pc.pipe)
    }
  }

  def fetchAndStorePRCommits(
      s: Session[IO]
  )(prs: fs2.Stream[IO, PRData]): fs2.Stream[IO, Completion] = {
    fs2.Stream.eval(s.prepare(insertPRCommit)).flatMap { pc =>
      prCommitsStream(prs)
        .through(pc.pipe)
    }
  }

  def fetchAndStoreAll(s: Session[IO]): IO[Unit] = {

    val prs = prStream()
    fetchAndStorePRCommits(s)(prs)
      .flatMap(_ => storePR(s)(prs))
      .compile
      .drain
      .onError(IO.println)
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
      val cli = new PRFetchSvc(client)
      cli.fetchAndStoreAll(s)
    }

  }
}
