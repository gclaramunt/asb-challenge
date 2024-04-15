package gclaramunt.asbchallenge.prservice

import cats.effect.IO
import fs2.Chunk
import gclaramunt.asbchallenge.Config
import gclaramunt.asbchallenge.Config.GitHubConfig
import github4s.{GHResponse, GithubClient}
import github4s.domain.{PRFilterClosed, PRFilterOpen, Pagination, PullRequest}
import org.http4s.client.{Client, JavaNetClientBuilder}
object PRFetchSvc {

  lazy  val httpClient: Client[IO] =
    JavaNetClientBuilder[IO].create // You can use any http4s backend

  private def getPRs(
      user: String,
      repo: String,
      page: Int
  ): IO[List[PullRequest]] = {

    val filter = List(PRFilterOpen, PRFilterClosed)

    val GitHubConfig(token, _, pageSize) = Config.asbConfig.github

    val pagination = if (page < 0) None else Some(Pagination(page, pageSize))
    GithubClient[IO](httpClient, Some(token)).pullRequests
      .listPullRequests(user, repo, filters = filter, pagination = pagination)
      .flatMap {
        case GHResponse(Right(prList), _, _) => IO.pure(prList)
        case GHResponse(Left(error), _, _)   => IO.raiseError(error)
      }

  }

  def prStream(): fs2.Stream[IO, PullRequest] =
    fs2.Stream.emits(Config.asbConfig.github.repositories).flatMap {

      case (user, repo) =>
        fs2.Stream.unfoldChunkEval(0) { page =>
          for {
            prs <- getPRs(user, repo, page)
          } yield {
            val nextPage = if (prs.nonEmpty) page + 1 else 0
            if (nextPage > 0) {
              Some((Chunk.from(prs.toIndexedSeq), nextPage))
            } else {
              None
            }
          }
        }

    }

  def fetchAndStorePR() = {

  }
}
