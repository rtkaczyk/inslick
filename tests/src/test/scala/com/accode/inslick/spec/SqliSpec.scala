package com.accode.inslick.spec

import com.accode.inslick.data.Animal
import com.accode.inslick.db.DbProvider
import zio.test.Assertion.equalTo
import zio.test._

abstract class SqliSpec(db: Db) extends DefaultRunnableSpec {
  val provider = new DbProvider(db.path)
  import db.api._
  import provider._

  def spec = suite(s"SqliInterpolator for ${db.path}")(
    testM("select all") {

      val values = Animal.all.map(_.tuple)

      val query =
        sqli"""select count(*) from animal a
               where (a.id, a.name, a.kind, a.legs, a.has_tail, created, updated) in *$values"""
          .as[Int]

      val result = for {
        _   <- dao.drop
        _   <- dao.create
        _   <- dao.insertAll(Animal.all)
        res <- query
        rows = res.headOption.getOrElse(0)
      } yield assert(rows)(equalTo(Animal.all.size))

      result.zio
    }
  )
}
