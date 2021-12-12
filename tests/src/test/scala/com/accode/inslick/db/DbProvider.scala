package com.accode.inslick.db
import slick.basic.DatabaseConfig
import slick.jdbc.{JdbcProfile, SetParameter}
import zio.Task

import java.sql.{Date, Timestamp}
import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext

class DbProvider(configPath: String) {
  private val dbConfig = DatabaseConfig.forConfig[JdbcProfile](configPath)

  val database: JdbcProfile#Backend#Database = dbConfig.db
  val profile: JdbcProfile                   = dbConfig.profile
  val name: String                           = dbConfig.profileName

  import profile.api._

  implicit val dbEC: ExecutionContext = database.executor.executionContext

  val dao: AnimalDao = new AnimalDao(profile)

  implicit class RunAction[R](dbio: DBIO[R]) {
    def zio: Task[R] = Task.fromFuture(_ => database.run(dbio))
  }

  implicit val localDateSP: SetParameter[LocalDate] =
    SetParameter((d, pp) => pp.setDate(Date.valueOf(d)))

  implicit val localDateTimeSP: SetParameter[LocalDateTime] =
    SetParameter((d, pp) => pp.setTimestamp(Timestamp.valueOf(d)))
}
