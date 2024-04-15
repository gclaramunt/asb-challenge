package gclaramunt.asbchallenge

import cats.Applicative
import cats.effect.Async
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

trait ProjectMetrics[F[_]] {
  def get(id: String): F[ProjectMetrics.Aggregation]
}

object ProjectMetrics {

  //do we really need it?
  def apply[F[_]](implicit ev: ProjectMetrics[F]): ProjectMetrics[F] = ev

  final case class Aggregation(
      totalContributors: Long,
      totalCommits: Long,
      totalClosedPRs: Long,
      totalOpenPRs: Long
  )

  def impl[F[_]: Async]: ProjectMetrics[F] = new ProjectMetrics[F] {
    override def get(id: String): F[Aggregation] =
      Applicative[F].pure(Aggregation(0, 0, 0, 0))
  }

  //move encoders outside
  object Aggregation {

    implicit val aggrDec: Decoder[Aggregation] = deriveDecoder[Aggregation]
    implicit val aggrEnc: Encoder[Aggregation] = deriveEncoder[Aggregation]

  }
}
