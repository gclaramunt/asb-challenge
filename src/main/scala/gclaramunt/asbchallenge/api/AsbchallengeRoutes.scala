package gclaramunt.asbchallenge.api

import cats.effect.{Concurrent}
import cats.syntax.all._
import gclaramunt.asbchallenge.{ContributorMetrics, ProjectMetrics, PullRequestFromWH, StoreService}
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

object AsbchallengeRoutes {

  def queryProjectRoutes[F[_]: Concurrent](PM: ProjectMetrics[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    val basePath = Root / "projects"
    HttpRoutes.of[F] {
      case GET -> `basePath` / projectRepo / projectUser / "metrics" =>
        for {
          metrics <- PM.get(projectUser, projectRepo)
          resp <- Ok(metrics)
        } yield resp
    }
  }

  def queryContributorRoutes[F[_]: Concurrent](
      CM: ContributorMetrics[F]
  ): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    val basePath = Root / "contributors"
    HttpRoutes.of[F] { case GET -> `basePath` / contributorLogin / "metrics" =>
      for {
        metrics <- CM.get(contributorLogin)
        resp <- Ok(metrics)
      } yield resp
    }
  }

  def insertRoutes[F[_]: Concurrent](SV: StoreService[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl._
    HttpRoutes.of[F] {
      case req@POST -> Root =>

        for {
          pr <- req.as[PullRequestFromWH]
          _ <- SV.storePR(pr)
          resp <- Ok()
        } yield resp
    }
  }

}
