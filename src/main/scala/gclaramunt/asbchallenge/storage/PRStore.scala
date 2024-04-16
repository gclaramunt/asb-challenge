package gclaramunt.asbchallenge.storage

import gclaramunt.asbchallenge.{ContributorMetrics, PRData, ProjectMetrics}
import skunk.{*:, Command, EmptyTuple, Query}
import skunk.codec.all.{int8, varchar}
import skunk.implicits.toStringOps

object PRStore {

  val contributorQ: Query[Long, ContributorMetrics.Aggregation] =
    sql"""
    SELECT
    count (DISTINCT (project_user, project_repo)),
    SUM(CASE WHEN status = 'open' THEN 1 ELSE 0 END) AS open_count,
    SUM(CASE WHEN status = 'closed' THEN 1 ELSE 0 END) AS closed_count
    FROM pull_request
    where user_id= $int8
    group by user_id;
  """.query(int8 *: int8 *: int8).to[ContributorMetrics.Aggregation]

  val projectQ
      : Query[String *: String *: EmptyTuple, ProjectMetrics.Aggregation] =
    sql"""
    SELECT
    count (DISTINCT (user_id)),
    SUM(CASE WHEN status = 'open' THEN 1 ELSE 0 END) AS open_count,
    SUM(CASE WHEN status = 'closed' THEN 1 ELSE 0 END) AS closed_count
    FROM pull_request
    WHERE  project_user = $varchar AND project_repo = $varchar
    group by project_user, project_repo;
  """.query(int8 *: int8 *: int8).to[ProjectMetrics.Aggregation]

  val insert: Command[PRData] =
    sql"""
          INSERT INTO pull_request
(id, project_repo, project_user, user_id, status)
VALUES($int8, $varchar, $varchar, $int8, $varchar)
ON CONFLICT DO NOTHING;
      """.command.to[PRData]

}
