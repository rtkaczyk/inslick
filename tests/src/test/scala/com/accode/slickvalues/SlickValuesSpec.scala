package com.accode.slickvalues

import com.accode.slickvalues.api._
import com.accode.slickvalues.db.{Animal, DbProvider}
import zio.test._
import zio.test.Assertion.equalTo

abstract class SlickValuesSpec(path: String) extends DefaultRunnableSpec {
  val provider = new DbProvider(path)
  import provider._

  def spec = suite(s"SqlVInterpolator for $name")(
    testM("simple query") {

      implicit val svp: SetValuesParameter[List[Animal.Tuple]] = setValues

      val values = Animal.all.map(_.tuple)

      val query =
        sqlv"""select count(*) from animal a
               where (a.id, a.name, a.kind, a.legs, a.has_tail) in (values $values)"""
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
