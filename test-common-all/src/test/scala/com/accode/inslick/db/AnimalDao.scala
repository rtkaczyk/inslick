package com.accode.inslick.db
import com.accode.inslick.data.Animal
import slick.dbio.DBIO

trait AnimalDao {
  val create: DBIO[Unit]
  val drop: DBIO[Unit]

  def insertAll(animals: Iterable[Animal]): DBIO[Unit]
}
