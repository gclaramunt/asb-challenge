package gclaramunt.asbchallenge.api

import cats.effect.Sync
import cats.syntax.all._
import gclaramunt.asbchallenge.{ContributorMetrics, ProjectMetrics}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object AsbchallengeRoutes {

  def projectRoutes[F[_]: Sync](PM: ProjectMetrics[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    val basePath = Root / "projects"
    HttpRoutes.of[F] { case GET -> `basePath` / projectId / "metrics" =>
      for {
        metrics <- PM.get(projectId)
        resp <- Ok(metrics)
      } yield resp
    }
  }

  def contributorRoutes[F[_]: Sync](
      CM: ContributorMetrics[F]
  ): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    val basePath = Root / "contributors"
    HttpRoutes.of[F] { case GET -> `basePath` / contributorId / "metrics" =>
      for {
        metrics <- CM.get(contributorId)
        resp <- Ok(metrics)
      } yield resp
    }
  }
}
