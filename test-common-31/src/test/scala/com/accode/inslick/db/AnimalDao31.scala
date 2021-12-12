package com.accode.inslick.db
import com.accode.inslick.data.Animal
import slick.driver.JdbcProfile

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext

class AnimalDao31(val profile: JdbcProfile)(implicit ec: ExecutionContext)
    extends AnimalDao
    with Mapping {

  import profile.api._

  class TblAnimal(tag: Tag) extends Table[Animal](tag, "animal") {
    def id      = column[Int]("id", O.PrimaryKey)
    def name    = column[String]("name")
    def kind    = column[String]("kind")
    def legs    = column[Int]("legs")
    def hasTail = column[Boolean]("has_tail")
    def created = column[LocalDate]("created")
    def updated = column[LocalDateTime]("updated", O.SqlType("timestamp"))

    def * = (id, name, kind, legs, hasTail, created, updated) <>
      ((Animal.apply _).tupled, Animal.unapply)
  }

  private val tblAnimal = TableQuery[TblAnimal]
  private val tblName   = tblAnimal.baseTableRow.tableName

  val create: DBIO[Unit] = tblAnimal.schema.create
  val drop: DBIO[Unit] =
    tblAnimal.schema.drop.asTry.map(_ => ()) // sqlu"drop table if exists #$tblName".map(_ => ())

  def insertAll(animals: Iterable[Animal]): DBIO[Unit] =
    (tblAnimal ++= animals).map(_ => ())
}
