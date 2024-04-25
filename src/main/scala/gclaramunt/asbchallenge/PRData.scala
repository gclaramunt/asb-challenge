package gclaramunt.asbchallenge

import github4s.domain.{PullRequest, Repository}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}


case class PRData(
    id: Int,
    projectUser: String,
    projectRepo: String,
    userId: String,
    status: String
)

object PRData {
  def from(
            whPr: PullRequestFromWH
  ): PRData = {
    from(whPr.repository.organization.map(_.login).getOrElse(whPr.repository.owner.login ),whPr.repository.name, whPr.pullRequest)
  }

  def from(
            projectUser: String,
            projectRepo: String,
            ghPr: PullRequest
          ): PRData =
    PRData(ghPr.number, projectUser, projectRepo, ghPr.user.get.login, ghPr.state)
}

case class PRUserCommit(sha: String, login: String, pullRequestId: Int)

object PRUserCommit {

  import cats.syntax.all._
  import io.circe.{ACursor, Decoder}

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


case class PullRequestFromWH(action: String, pullRequest: PullRequest, repository: Repository )
object PullRequestFromWH {


  import github4s.Decoders._
  import github4s.Encoders._

  implicit val aggrEnc: Encoder[PullRequestFromWH] = deriveEncoder[PullRequestFromWH]
  implicit val aggrDec: Decoder[PullRequestFromWH] = deriveDecoder[PullRequestFromWH]
}