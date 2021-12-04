package com.accode.slickvalues.data
import com.accode.slickvalues.data.AnimalDao.Animal
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile

abstract class AnimalDao(dbConfig: DatabaseConfig[JdbcProfile]) {
  import dbConfig.profile.api._

  class TblAnimal(tag: Tag) extends Table[Animal](tag, "animal") {
    def id      = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name    = column[String]("name")
    def group   = column[String]("group")
    def legs    = column[Int]("legs")
    def hasTail = column[Boolean]("hasTail")

    def * = (id, name, group, legs, hasTail) <> ((Animal.apply _).tupled, Animal.unapply)
  }

  private val tblAnimal = TableQuery[TblAnimal]

  val create: DBIO[Unit] = tblAnimal.schema.create
  val drop: DBIO[Unit]   = tblAnimal.schema.drop
}

object AnimalDao {
  case class Animal(id: Long, name: String, group: String, legs: Int, hasTail: Boolean)
}
