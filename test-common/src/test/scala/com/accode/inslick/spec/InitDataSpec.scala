package com.accode.inslick.spec
import com.accode.inslick.data.Animal
import com.accode.inslick.slick.AnimalDao
import com.accode.inslick.slick.Slick33
import zio.Task
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

object InitDataSpec extends DefaultRunnableSpec {

  def spec = suite("InitData")(
    tests: _*
  ) @@ parallel

  def tests = DbDef.all.map { db =>
    val n = Animal.all.size
    testM(s"insert $n records into ${db.path}") {
      initData(Slick33(db.path))
        .map(r => assert(r)(equalTo(n)))
    }
  }

  def initData(slick33: Slick33): Task[Int] = {
    import slick33._
    val dao = new AnimalDao(slick33)

    val run = for {
      _ <- dao.drop
      _ <- dao.create
      r <- dao.insertAll(Animal.all)
    } yield r

    run.zio
  }
}
