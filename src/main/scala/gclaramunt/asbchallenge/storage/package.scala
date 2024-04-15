package gclaramunt.asbchallenge

import cats.effect.Temporal
import cats.effect.std.Console
import fs2.io.net.Network
import gclaramunt.asbchallenge.Config.DbConfig
import natchez.Trace
import skunk.Session

package object storage {
  def session[F[_]: Temporal: Trace: Network: Console] = {
    val DbConfig(host, port, user, password, database, maxConnections) =
      Config.asbConfig.dbConfig

    Session.pooled(
      host = host,
      port = port,
      user = user,
      database = database,
      password = password,
      max = maxConnections
    )
  }


}
