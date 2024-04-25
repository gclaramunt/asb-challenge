package gclaramunt.asbchallenge

import cats.FlatMap
import gclaramunt.asbchallenge.storage.PRStore.insertPR

import skunk.Session
import cats.syntax.all._

trait StoreService[F[_]] {
  def storePR(ghPR: PullRequestFromWH):F[Unit]
}


object StoreServiceImpl {
  def impl[F[_]: FlatMap](s: Session[F]) = new StoreService[F] {
    override def storePR(pr: PullRequestFromWH): F[Unit] = for {
      stm <- s.prepare(insertPR)
      _ <- stm.execute(PRData.from(pr))
    } yield ()


  }
}
