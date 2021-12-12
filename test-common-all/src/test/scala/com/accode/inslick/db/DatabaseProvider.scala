package com.accode.inslick.db
import com.accode.inslick.db.DatabaseProvider._
import slick.dbio.DBIO
import slick.jdbc.SQLActionBuilder
import zio.Task

import scala.concurrent.ExecutionContext

trait DatabaseProvider {
  val dao: AnimalDao

  implicit val dbEC: ExecutionContext

  implicit def runAction[R]: DBIO[R] => RunAction[R]

  implicit def sqlAction: SQLActionBuilder => SqlAction
}

object DatabaseProvider {
  trait Factory extends (String => DatabaseProvider)

  trait RunAction[R] {
    def zio: Task[R]
  }

  trait SqlAction {
    def count: DBIO[Int]
  }
}
