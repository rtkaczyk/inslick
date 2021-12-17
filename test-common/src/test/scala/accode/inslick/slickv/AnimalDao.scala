package accode.inslick.slickv

import accode.inslick.data.Animal
import slick.sql.SqlProfile.ColumnOption.SqlType

import java.time.{LocalDate, LocalDateTime}

class AnimalDao(val slick33: Slick33) {
  import slick33._
  import profile.api._

  class TblAnimal(tag: Tag) extends Table[Animal](tag, "animal") {
    def id      = column[Long]("id", O.PrimaryKey)
    def name    = column[String]("name")
    def kind    = column[String]("kind")
    def alias   = column[Option[String]]("alias")
    def legs    = column[Int]("legs")
    def hasTail = column[Boolean]("has_tail")
    def created = column[LocalDate]("created")
    def updated = column[LocalDateTime]("updated", SqlType("timestamp"))

    def * = (id, name, kind, alias, legs, hasTail, created, updated) <>
      ((Animal.apply _).tupled, Animal.unapply)
  }

  private val tblAnimal = TableQuery[TblAnimal]

  val create: DBIO[Unit] = tblAnimal.schema.create
  val drop: DBIO[Unit]   = tblAnimal.schema.dropIfExists

  def insertAll(animals: Iterable[Animal]): DBIO[Int] =
    (tblAnimal ++= animals).map(_.getOrElse(0))
}
