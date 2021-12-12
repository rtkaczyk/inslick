package com.accode.inslick.db
import com.accode.inslick.db.DatabaseProvider.{RunAction, SqlAction}
import slick.backend.DatabaseConfig
import slick.dbio.DBIO
import slick.driver.JdbcProfile
import slick.jdbc.SQLActionBuilder
import zio.Task

class DatabaseProvider30(configPath: String) extends DatabaseProvider {
  private val dbConfig = DatabaseConfig.forConfig[JdbcProfile](configPath)
  private val database = dbConfig.db

  implicit val dbEC = database.executor.executionContext

  implicit def runAction[R] = (dbio: DBIO[R]) =>
    new RunAction[R] {
      def zio: Task[R] = Task.fromFuture(_ => database.run(dbio))
    }

  implicit def sqlAction = (sql: SQLActionBuilder) =>
    new SqlAction {
      def count = sql.as[Int].map(_.headOption.getOrElse(0))
    }

  val dao = new AnimalDao31(dbConfig.driver)
}

object DatabaseProvider30 extends DatabaseProvider.Factory {
  def apply(path: String) = new DatabaseProvider30(path)
}
