package accode.inslick.slickv
import accode.inslick.data.{Animal, Person}

class PersonDao(val slick33: Slick33) {
  import slick33._
  import profile.api._

  class TblPerson(tag: Tag) extends Table[Person](tag, "person") {
    def id        = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def firstName = column[Option[String]]("first_name")
    def lastName  = column[Option[String]]("last_name")
    def shoeSize  = column[Option[Int]]("shoe_size")

    def * = (id, firstName, lastName, shoeSize) <>
      ((Person.apply _).tupled, Person.unapply)
  }

  private val tblAnimal = TableQuery[TblPerson]

  val create: DBIO[Unit] = tblAnimal.schema.create
  val drop: DBIO[Unit]   = tblAnimal.schema.dropIfExists
}
