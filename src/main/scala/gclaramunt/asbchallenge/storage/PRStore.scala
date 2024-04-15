package gclaramunt.asbchallenge.storage

import gclaramunt.asbchallenge.{ContributorMetrics, PRData, ProjectMetrics}
import skunk.{Command, Query}
import skunk.codec.all.{int8, varchar}
import skunk.implicits.toStringOps

object PRStore {

  val contributorsQ: Query[String, ContributorMetrics.Aggregation] =
    sql"""
    SELECT  user_id,
    count (DISTINCT (project_repo, project_user)),
    SUM(CASE WHEN status = 'open' THEN 1 ELSE 0 END) AS open_count,
    SUM(CASE WHEN status = 'closed' THEN 1 ELSE 0 END) AS closed_count
    FROM public.pull_request
    where user_id= $varchar
    group by user_id;
  """.query(int8 *: int8 *: int8 *: int8)
      .to[ContributorMetrics.Aggregation]

  val projectsQ: Query[String, ProjectMetrics.Aggregation] =
    sql"""
    SELECT name, population
    FROM   pull_request
    WHERE  user_id LIKE $varchar
  """.query(int8 *: int8 *: int8 *: int8).to[ProjectMetrics.Aggregation]

  val insert: Command[PRData] =
    sql"""
          INSERT INTO pull_request
(project_repo, user_id, status, project_user)
VALUES($varchar, $varchar, $varchar);
      """.command.to[PRData]

}
