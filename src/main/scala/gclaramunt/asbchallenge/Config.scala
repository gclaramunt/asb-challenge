package gclaramunt.asbchallenge

import pureconfig.ConfigSource
import pureconfig.generic.auto._

object Config {
  lazy val asbConfig: AsbConfig = ConfigSource
    .default
    .at("asb")
    .loadOrThrow[AsbConfig]
  
  
  case class AsbConfig(github: GitHubConfig, dbConfig: DbConfig, server: ServerConfig)
  
  case class GitHubConfig(token: String, repositories: Seq[(String, String)], pageSize: Int)

  case class ServerConfig(host: String, port: Int)

  case class DbConfig(host: String, port: Int, user: String, password: Option[String], database: String, maxSessions: Int)
}
