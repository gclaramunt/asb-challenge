package gclaramunt.asbchallenge.api

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {
  val run = AsbchallengeServer.run[IO]
}
