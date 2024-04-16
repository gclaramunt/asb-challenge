package gclaramunt.asbchallenge

import cats.FlatMap
import gclaramunt.asbchallenge.storage.PRStore.projectQ
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import skunk.Session
import skunk._

trait ProjectMetrics[F[_]] {
  def get(
      projectUser: String,
      projectRepo: String
  ): F[ProjectMetrics.Aggregation]
}

object ProjectMetrics {

  //do we really need it?
  def apply[F[_]](implicit ev: ProjectMetrics[F]): ProjectMetrics[F] = ev

  final case class Aggregation(
      totalContributors: Long,
      //totalCommits: Long,
      totalOpenPRs: Long,
      totalClosedPRs: Long
  )

  def impl[F[_]: FlatMap](s: Session[F]): ProjectMetrics[F] =
    new ProjectMetrics[F] {
      val query = s.prepare(projectQ)
      override def get(
          projectUser: String,
          projectRepo: String
      ): F[Aggregation] =
        FlatMap[F].flatMap(query)(_.unique((projectUser, projectRepo)))
    }

  //move encoders outside
  object Aggregation {

    implicit val aggrDec: Decoder[Aggregation] = deriveDecoder[Aggregation]
    implicit val aggrEnc: Encoder[Aggregation] = deriveEncoder[Aggregation]

  }
}
