package gclaramunt.asbchallenge

import cats.Applicative
import cats.effect.{Async, Concurrent}
import io.circe.{Decoder, Encoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

trait ProjectMetrics[F[_]]:
  def get(id: String): F[ProjectMetrics.Aggregation]

object ProjectMetrics:

  //do we really need it? 
  def apply[F[_]](using ev: ProjectMetrics[F]): ProjectMetrics[F] = ev
  
  final case class Aggregation(totalContributors: Long, totalCommits: Long, totalClosedPRs: Long, totalOpenPRs: Long)

  def impl[F[_]: Async]: ProjectMetrics[F] = new ProjectMetrics[F]:
    override def get(id: String): F[Aggregation] = Applicative[F].pure(Aggregation(0,0,0,0))

  //move enconders outside
  object Aggregation:
    given Decoder[Aggregation] = Decoder.derived[Aggregation]
    given [F[_] : Concurrent]: EntityDecoder[F, Aggregation] = jsonOf
    given Encoder[Aggregation] = Encoder.AsObject.derived[Aggregation]
    given [F[_]]: EntityEncoder[F, Aggregation] = jsonEncoderOf