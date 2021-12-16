package accode.inslick.slickv

import slick.jdbc.{GetResult, SQLActionBuilder}
import zio.Task

import scala.concurrent.Future
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

trait Slick {
  protected def sqlAction[R: GetResult](sql: SQLActionBuilder): Future[Vector[R]]

  implicit class SqlAction(sql: SQLActionBuilder) {
    def zio[R: GetResult]: Task[Vector[R]] = Task.fromFuture(_ => sqlAction[R](sql))
    def count: Task[Int]                   = zio[Int].map(_.headOption.getOrElse(0))
  }
}

object Slick {
  trait Factory extends (String => Slick) {
    val version: String
  }

  def apply(version: String): Slick.Factory = macro makeSlick

  def makeSlick(c: blackbox.Context)(version: c.Tree): c.Tree = {
    import c.universe._

    val Literal(Constant(versionStr: String)) = version
    val imports =
      if (versionStr.matches("^3\\.[01].*"))
        q"""
        import slick.backend.DatabaseConfig
        import slick.driver.JdbcProfile
       """
      else
        q"""
        import slick.basic.DatabaseConfig
        import slick.jdbc.JdbcProfile
       """

    q"""
      {
        ..$imports
        import slick.jdbc.{GetResult, SQLActionBuilder}
        
        class Slick3x(configPath: String) extends Slick {
          private val dbConfig = DatabaseConfig.forConfig[JdbcProfile](configPath)
          protected def sqlAction[R: GetResult](sql: SQLActionBuilder) = dbConfig.db.run(sql.as[R])
        }
        new Slick.Factory {
          def apply(path: String) = new Slick3x(path)
          val version = $version
        }
      }
     """
  }
}
