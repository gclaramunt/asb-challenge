package gclaramunt.asbchallenge.storage

import gclaramunt.asbchallenge.{ContributorMetrics, PRData, PRUserCommit, ProjectMetrics}
import skunk.codec.all.{int4, int8, varchar}
import skunk.implicits.toStringOps
import skunk.{*:, Command, EmptyTuple, Query}

object PRStore {

  val contributorQ
      : Query[String *: String *: EmptyTuple, ContributorMetrics.Aggregation] =
    sql"""
SELECT
    COUNT(DISTINCT(project_user, project_repo)) AS repos,
    SUM(commit_count) :: int8 AS total_commits ,
    SUM(CASE WHEN status = 'open' THEN 1 ELSE 0 END) AS open_count,
    SUM(CASE WHEN status = 'closed' THEN 1 ELSE 0 END) AS closed_count
FROM
    (SELECT
         pr.project_user,
         pr.project_repo,
         pr.status,
         pr.id,
        COUNT(sha)  AS commit_count
     FROM
         pull_request pr, pr_commit pc
     WHERE
          pr.id = pc.pull_request_id and
         ( pc.login  = $varchar or pr.login =  $varchar )
         group by  pr.project_repo, pr.project_user , pr.status, pr.id
         ) AS subquery
    """.query(int8 *: int8 *: int8 *: int8).to[ContributorMetrics.Aggregation]

  val projectQ
      : Query[String *: String *: EmptyTuple, ProjectMetrics.Aggregation] =
    sql"""
SELECT
    COUNT(DISTINCT COALESCE(login, contributor)) AS contributors,
    SUM(commit_count):: int8 AS total_commits,
    SUM(CASE WHEN status = 'open' THEN 1 ELSE 0 END) AS open_count,
    SUM(CASE WHEN status = 'closed' THEN 1 ELSE 0 END) AS closed_count
FROM
    (SELECT
         pr.project_user,
         pr.project_repo,
         pr.status,
         pr.id,
         pr.login,
         pc.login as contributor,
        COUNT(sha)  AS commit_count
     FROM
         pull_request pr, pr_commit pc
     WHERE
          pr.id = pc.pull_request_id
          and pr.project_user = $varchar and pr.project_repo = $varchar
         group by  pr.project_repo, pr.project_user , pr.status, pr.id, contributor
         ) AS subquery
group by project_repo, project_user
    """.query(int8 *: int8 *: int8 *: int8).to[ProjectMetrics.Aggregation]

  val insertPR: Command[PRData] =
    sql"""
      INSERT INTO pull_request
      (id, project_user, project_repo, login, status)
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
