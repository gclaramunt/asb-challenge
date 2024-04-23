package gclaramunt.asbchallenge

import github4s.domain.PullRequest
import io.circe.Decoder
//import io.circe.Decoder

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

case class PRUserCommit(login: String, pullRequestId: Int)

object PRUserCommit {
  implicit val decodeCommit: Decoder[PRUserCommit] = Decoder.instance { c =>
    for {
      id <- c.downField("author").downField("login").as[String]
    } yield PRUserCommit(id, -1)
  }
}
