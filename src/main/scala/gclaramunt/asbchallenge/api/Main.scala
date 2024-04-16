package gclaramunt.asbchallenge.api

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple {
  import natchez.Trace.Implicits.noop
  val run = AsbchallengeServer.run[IO]
}
