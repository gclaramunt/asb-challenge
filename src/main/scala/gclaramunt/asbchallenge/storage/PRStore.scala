package gclaramunt.asbchallenge.storage

import gclaramunt.asbchallenge.{
  ContributorMetrics,
  PRData,
  PRUserCommit,
  ProjectMetrics
}
import skunk.{*:, Command, EmptyTuple, Query}
import skunk.codec.all.{int4, int8, varchar}
import skunk.implicits.toStringOps

object PRStore {

  val contributorQ: Query[String, ContributorMetrics.Aggregation] =
    sql"""
      SELECT
      count (DISTINCT (project_user, project_repo)),
       count (c.login),
      SUM(CASE WHEN status = 'open' THEN 1 ELSE 0 END) AS open_count,
      SUM(CASE WHEN status = 'closed' THEN 1 ELSE 0 END) AS closed_count
      FROM pull_request pr , pr_commit c
      where login= $varchar
      and c.pull_request_id = pr.id
      group by login;
    """.query(int8 *: int8 *: int8 *: int8).to[ContributorMetrics.Aggregation]

  val projectQ
      : Query[String *: String *: EmptyTuple, ProjectMetrics.Aggregation] =
    sql"""
      SELECT
      count (DISTINCT (c.login)),
      count (c.login),
      SUM(CASE WHEN status = 'open' THEN 1 ELSE 0 END) AS open_count,
      SUM(CASE WHEN status = 'closed' THEN 1 ELSE 0 END) AS closed_count
      FROM pull_request pr, pr_commit c
      WHERE  project_user = $varchar AND project_repo = $varchar
      and c.pull_request_id = pr.id
      group by project_user, project_repo;
    """.query(int8 *: int8 *: int8 *: int8).to[ProjectMetrics.Aggregation]

  val insertPR: Command[PRData] =
    sql"""
      INSERT INTO pull_request
      (id, project_repo, project_user, login, status)
      VALUES($int4, $varchar, $varchar, $varchar, $varchar)
      ON CONFLICT DO NOTHING;
    """.command.to[PRData]

  val insertPRCommit: Command[PRUserCommit] =
    sql"""
      INSERT INTO pr_commit
      (sha, login, pull_request_id)
      VALUES($varchar, $varchar, $int4)
      ON CONFLICT DO NOTHING;
    """.command.to[PRUserCommit]
}
