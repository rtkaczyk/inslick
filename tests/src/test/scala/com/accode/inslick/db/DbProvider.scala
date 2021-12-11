package com.accode.inslick.db
import slick.basic.DatabaseConfig
import slick.dbio.DBIO
import slick.jdbc.JdbcProfile
import zio.Task

import scala.concurrent.ExecutionContext

class DbProvider(configPath: String) {
  private val dbConfig = DatabaseConfig.forConfig[JdbcProfile](configPath)

  val db: JdbcProfile#Backend#Database = dbConfig.db
  val profile: JdbcProfile             = dbConfig.profile
  val name: String                     = dbConfig.profileName

  implicit val dbEC: ExecutionContext = db.executor.executionContext

  val dao: AnimalDao = new AnimalDao(profile)

  implicit class RunAction[R](dbio: DBIO[R]) {
    def zio: Task[R] = Task.fromFuture(_ => db.run(dbio))
  }
}
