package accode.inslick.data
import accode.inslick.data.Animal.NameKind

import java.time.{LocalDate, LocalDateTime}

case class Animal(
    id: Long,
    name: String,
    kind: String,
    alias: Option[String],
    legs: Int,
    hasTail: Boolean,
    created: LocalDate,
    updated: LocalDateTime
) {
  def tuple: Animal.Tuple = Animal.unapply(this).get

  def noOpt: (Long, String, String, Int, Boolean, LocalDate, LocalDateTime) =
    (id, name, kind, legs, hasTail, created, updated)

  def toNameKind: NameKind = NameKind(name, kind)
}

object Animal {
  type Tuple = (Long, String, String, Option[String], Int, Boolean, LocalDate, LocalDateTime)

  case class NameKind(name: String, kind: String)

  val ld: Long => LocalDate     = LocalDate.parse("2021-01-01").plusDays
  val dt: Long => LocalDateTime = LocalDateTime.parse("2021-02-01T00:00:00").plusDays

  val cat   = Animal(1, "cat", "mammal", Some("feline"), 4, true, ld(1), dt(1))
  val shark = Animal(2, "shark", "fish", None, 0, true, ld(2), dt(2))
  val human = Animal(3, "human", "mammal", Some("homo-sapiens"), 2, false, ld(3), dt(3))
  val snake = Animal(4, "snake", "reptile", None, 0, true, ld(4), dt(4))
  val fly   = Animal(5, "fly", "insect", None, 6, false, ld(5), dt(5))

  val all = List(cat, shark, human, snake, fly)
}
