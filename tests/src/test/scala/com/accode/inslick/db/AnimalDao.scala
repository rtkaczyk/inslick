package com.accode.inslick.db
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext

class AnimalDao(val profile: JdbcProfile)(implicit ec: ExecutionContext) {
  import profile.api._

  class TblAnimal(tag: Tag) extends Table[Animal](tag, "animal") {
    def id      = column[Int]("id", O.PrimaryKey)
    def name    = column[String]("name")
    def kind    = column[String]("kind")
    def legs    = column[Int]("legs")
    def hasTail = column[Boolean]("has_tail")

    def * = (id, name, kind, legs, hasTail) <> ((Animal.apply _).tupled, Animal.unapply)
  }

  private val tblAnimal = TableQuery[TblAnimal]

  val create: DBIO[Unit] = tblAnimal.schema.create
  val drop: DBIO[Unit]   = tblAnimal.schema.dropIfExists

  def insertAll(animals: Iterable[Animal]): DBIO[Unit] =
    (tblAnimal ++= animals).map(_.getOrElse(0))

}
