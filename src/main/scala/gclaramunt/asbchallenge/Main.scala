package gclaramunt.asbchallenge

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple:
  val run = AsbchallengeServer.run[IO]
