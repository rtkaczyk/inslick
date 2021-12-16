package accode.inslick.slickv
import slick.basic.DatabaseConfig
import slick.dbio.DBIO
import slick.jdbc.{GetResult, JdbcProfile, SQLActionBuilder}
import zio.Task

class Slick33(configPath: String) extends Slick {
  private val dbConfig = DatabaseConfig.forConfig[JdbcProfile](configPath)
  private val database = dbConfig.db
  protected def sqlAction[R: GetResult](sql: SQLActionBuilder) = database.run(sql.as[R])

  val profile = dbConfig.profile

  implicit val dbEC = database.executor.executionContext
  implicit class RunAction[R](dbio: DBIO[R]) {
    def zio: Task[R] = Task.fromFuture(_ => database.run(dbio))
  }
}

object Slick33 extends Slick.Factory {
  def apply(path: String) = new Slick33(path)
  val version             = "3.3"
}
