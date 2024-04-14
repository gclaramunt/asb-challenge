package gclaramunt.asbchallenge


import cats.effect.IO
import org.http4s.*
import org.http4s.implicits.*
import munit.CatsEffectSuite


class AsbChallengeRoutesSpec extends CatsEffectSuite:

    test("HelloWorld returns status code 200") {
        assertIO(retProject.map(_.status) ,Status.Ok)
    }

    test("HelloWorld returns hello world message") {
        assertIO(retProject.flatMap(_.as[String]), "{\"totalContributors\":0,\"totalCommits\":0,\"totalClosedPRs\":0,\"totalOpenPRs\":0}")
    }

    private[this] val retProject: IO[Response[IO]] =
        val getProject = Request[IO](Method.GET, uri"/projects/123/metrics")
        val pm = ProjectMetrics.impl[IO]
        AsbchallengeRoutes.projectRoutes(pm).orNotFound(getProject)

        