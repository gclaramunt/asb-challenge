package gclaramunt.asbchallenge.api

import cats.effect.Async
import cats.effect.std.Console
import cats.syntax.all._
import com.comcast.ip4s._
import fs2.io.net.Network
import gclaramunt.asbchallenge._
import gclaramunt.asbchallenge.storage.session
import natchez.Trace
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object AsbchallengeServer {

  def run[F[_]: Async: Trace: Network: Console]: F[Nothing] = (for {
    pool <- session
    s <- pool

    finalHttpApp = {
      val projectSvc = ProjectMetrics.impl[F](s)
      val contributorSvc = ContributorMetrics.impl[F](s)
      val storeSvc = StoreServiceImpl.impl[F](s)

      val httpApp = (
        AsbchallengeRoutes.queryProjectRoutes[F](projectSvc) <+>
          AsbchallengeRoutes.queryContributorRoutes[F](contributorSvc) <+>
          AsbchallengeRoutes.insertRoutes[F](storeSvc)
      ).orNotFound
      // With Middlewares in place
      Logger.httpApp(logHeaders = true, logBody = true)(httpApp)
    }
    _ <- EmberServerBuilder
      .default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(finalHttpApp)
      .build
  } yield ()).useForever
}
