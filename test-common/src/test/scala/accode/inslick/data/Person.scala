package accode.inslick.data

case class Person(
    id: Long,
    firstName: Option[String],
    lastName: Option[String],
    shoeSize: Option[Int]
) {
  def tuple: (Long, Option[String], Option[String], Option[Int]) = Person.unapply(this).get
}

object Person {
  val all = List(
    make(1, "John", "Smith", 44),
    make(2, "Jane", "Smith", 38),
    make(3, "John", "Doe", 42),
    make(4, "Jane", "Doe", 36)
  )

  def make(id: Long, f: String, l: String, s: Int): Person =
    Person(id, Some(f), Some(l), Some(s))
}
