package gclaramunt.asbchallenge

import cats.Applicative
import cats.effect.Async
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

trait ContributorMetrics[F[_]] {
  def get(id: String): F[ContributorMetrics.Aggregation]
}

object ContributorMetrics {

  //do we really need it?
  def apply[F[_]](implicit ev: ContributorMetrics[F]): ContributorMetrics[F] =
    ev

  final case class Aggregation(
      totalProjects: Long,
      totalCommits: Long,
      totalClosedPRs: Long,
      totalOpenPRs: Long
  )

  def impl[F[_]: Async]: ContributorMetrics[F] = new ContributorMetrics[F] {
    override def get(id: String): F[Aggregation] =
      Applicative[F].pure(Aggregation(0, 0, 0, 0))
  }

  //move encoders outside
  object Aggregation {

    implicit val aggrDec: Decoder[Aggregation] = deriveDecoder[Aggregation]
    implicit val aggrEnc: Encoder[Aggregation] = deriveEncoder[Aggregation]

  }
}
