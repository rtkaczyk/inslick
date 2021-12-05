package com.accode.slickvalues.db

case class Animal(id: Int, name: String, kind: String, legs: Int, hasTail: Boolean) {
  def tuple: Animal.Tuple = (id, name, kind, legs, hasTail)
}

object Animal {
  type Tuple = (Int, String, String, Int, Boolean)

  val cat   = Animal(1, "cat", "mammal", 4, true)
  val shark = Animal(2, "shark", "fish", 0, true)
  val human = Animal(3, "human", "mammal", 2, false)
  val snake = Animal(4, "snake", "reptile", 0, true)
  val fly   = Animal(5, "fly", "insect", 6, false)

  val all = List(cat, shark, human, snake, fly)
}
