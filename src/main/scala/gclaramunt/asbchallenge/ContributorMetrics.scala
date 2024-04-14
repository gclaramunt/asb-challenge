package gclaramunt.asbchallenge

import cats.Applicative
import cats.effect.{Async, Concurrent}
import io.circe.{Decoder, Encoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}

trait ContributorMetrics[F[_]]:
  def get(id: String): F[ContributorMetrics.Aggregation]

object ContributorMetrics:

  //do we really need it? 
  def apply[F[_]](using ev: ContributorMetrics[F]): ContributorMetrics[F] = ev
  
  final case class Aggregation(totalContributors: Long, totalCommits: Long, totalClosedPRs: Long, totalOpenPRs: Long)

  def impl[F[_]: Async]: ContributorMetrics[F] = new ContributorMetrics[F]:
    override def get(id: String): F[Aggregation] = Applicative[F].pure(Aggregation(0,0,0,0))

  //move enconders outside
  object Aggregation:
    given Decoder[Aggregation] = Decoder.derived[Aggregation]
    given [F[_] : Concurrent]: EntityDecoder[F, Aggregation] = jsonOf
    given Encoder[Aggregation] = Encoder.AsObject.derived[Aggregation]
    given [F[_]]: EntityEncoder[F, Aggregation] = jsonEncoderOf