package gclaramunt.asbchallenge

import cats.effect.Concurrent
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.{EntityDecoder, EntityEncoder}

package object api {

  implicit def cAggrEntityDec[F[_]: Concurrent]
      : EntityDecoder[F, ContributorMetrics.Aggregation] = jsonOf
  implicit def cAggreEntityEnc[F[_]]
      : EntityEncoder[F, ContributorMetrics.Aggregation] =
    jsonEncoderOf

  implicit def pAggrEntityDec[F[_]: Concurrent]
      : EntityDecoder[F, ProjectMetrics.Aggregation] = jsonOf
  implicit def pAggreEntityEnc[F[_]]
      : EntityEncoder[F, ProjectMetrics.Aggregation] =
    jsonEncoderOf

  implicit def pPRWHEntityDec[F[_]: Concurrent]
      : EntityDecoder[F, PullRequestFromWH] = jsonOf
  implicit def pPRWHEntityEnc[F[_]]: EntityEncoder[F, PullRequestFromWH] =
    jsonEncoderOf

}
