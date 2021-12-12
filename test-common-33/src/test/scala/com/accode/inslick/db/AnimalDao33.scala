package com.accode.inslick.db
import com.accode.inslick.data.Animal
import slick.jdbc.JdbcProfile
import slick.sql.SqlProfile.ColumnOption.SqlType

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.ExecutionContext

class AnimalDao33(val profile: JdbcProfile)(implicit ec: ExecutionContext)
    extends AnimalDao with Mapping {

  import profile.api.{localDateColumnType => _, localDateTimeColumnType => _, _}

  class TblAnimal(tag: Tag) extends Table[Animal](tag, "animal") {
    def id      = column[Int]("id", O.PrimaryKey)
    def name    = column[String]("name")
    def kind    = column[String]("kind")
    def legs    = column[Int]("legs")
    def hasTail = column[Boolean]("has_tail")
    def created = column[LocalDate]("created")
    def updated = column[LocalDateTime]("updated", SqlType("timestamp"))

    def * = (id, name, kind, legs, hasTail, created, updated) <>
      ((Animal.apply _).tupled, Animal.unapply)
  }

  private val tblAnimal = TableQuery[TblAnimal]

  val create: DBIO[Unit] = tblAnimal.schema.create
  val drop: DBIO[Unit]   = tblAnimal.schema.drop.asTry.map(_ => ())

  def insertAll(animals: Iterable[Animal]): DBIO[Unit] =
    (tblAnimal ++= animals).map(_.getOrElse(0))

}
