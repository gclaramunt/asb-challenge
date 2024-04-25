package gclaramunt.asbchallenge

import cats.effect.IO
import gclaramunt.asbchallenge.api.AsbchallengeRoutes
import org.http4s._
import org.http4s.implicits._
import munit.CatsEffectSuite

class AsbChallengeRoutesSpec extends CatsEffectSuite {

  test("project returns returns status code 200") {
    assertIO(retProject.map(_.status), Status.Ok)
  }

  test("project returns project metrics") {
    assertIO(
      retProject.flatMap(_.as[String]),
      "{\"totalContributors\":0,\"totalCommits\":0,\"totalClosedPRs\":0,\"totalOpenPRs\":0}"
    )
  }

  test("contributors returns returns status code 200") {
    assertIO(retContributor.map(_.status), Status.Ok)
  }

  test("contributors returns project metrics") {
    assertIO(
      retContributor.flatMap(_.as[String]),
      "{\"totalProjects\":0,\"totalCommits\":0,\"totalClosedPRs\":0,\"totalOpenPRs\":0}"
    )
  }

  private[this] val retProject: IO[Response[IO]] = {
    val getProject = Request[IO](Method.GET, uri"/projects/user1/proj1/metrics")
    val pm = new ProjectMetrics[IO] {
      override def get(
          projectUser: String,
          projectRepo: String
      ): IO[ProjectMetrics.Aggregation] =
        IO.pure(ProjectMetrics.Aggregation(0, 0, 0, 0))
    }
    AsbchallengeRoutes.queryProjectRoutes(pm).orNotFound(getProject)
  }

  private[this] val retContributor: IO[Response[IO]] = {
    val getContributor = Request[IO](Method.GET, uri"/contributors/anuser/metrics")
    val cm = new ContributorMetrics[IO] {
      override def get(login: String): IO[ContributorMetrics.Aggregation] =
        IO.pure(ContributorMetrics.Aggregation(0, 0, 0, 0))
    }
    AsbchallengeRoutes.queryContributorRoutes(cm).orNotFound(getContributor)
  }

}
