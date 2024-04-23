package gclaramunt.asbchallenge

import github4s.domain.PullRequest


case class PRData(
    id: Int,
    projectUser: String,
    projectRepo: String,
    userId: String,
    status: String
)

object PRData {
  def from(
      projectUser: String,
      projectRepo: String,
      ghPr: PullRequest
  ): PRData =
    PRData(ghPr.number, projectUser, projectRepo, ghPr.user.get.login, ghPr.state)
}

case class PRUserCommit(sha: String, login: String, pullRequestId: Int)

object PRUserCommit {

  import io.circe.{ACursor, Decoder}
  import cats.syntax.all._

  private def attemptExtractName(c: ACursor) = {
    c.downField("author").downField("login").as[String].orElse(c.downField("author").downField("name").as[String])
  }
  implicit val decodeCommit: Decoder[PRUserCommit] = Decoder.instance { c =>

    for {
      sha <- c.downField("sha").as[String]
      id <- attemptExtractName(c).leftFlatMap{ _ => attemptExtractName(c.downField("commit")) }
    } yield PRUserCommit(sha, id, -1)
  }
}
