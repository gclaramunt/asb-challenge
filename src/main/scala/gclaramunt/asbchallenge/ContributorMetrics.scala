package gclaramunt.asbchallenge

import cats.FlatMap
import cats.syntax.all._
import gclaramunt.asbchallenge.storage.PRStore.contributorQ
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import skunk.Session
import skunk._

trait ContributorMetrics[F[_]] {
  def get(login: String): F[ContributorMetrics.Aggregation]
}

object ContributorMetrics {

  //do we really need it?
  def apply[F[_]](implicit ev: ContributorMetrics[F]): ContributorMetrics[F] =
    ev

  final case class Aggregation(
      totalProjects: Long,
      totalCommits: Long,
      totalOpenPRs: Long,
      totalClosedPRs: Long
  )

  def impl[F[_]: FlatMap](s: Session[F]): ContributorMetrics[F] =
    new ContributorMetrics[F] {

      val query = s.prepare(contributorQ)
      override def get(login: String): F[Aggregation] =
        query.flatMap(_.unique((login, login)))
    }

  //move encoders outside
  object Aggregation {

    implicit val aggrDec: Decoder[Aggregation] = deriveDecoder[Aggregation]
    implicit val aggrEnc: Encoder[Aggregation] = deriveEncoder[Aggregation]

  }
}
