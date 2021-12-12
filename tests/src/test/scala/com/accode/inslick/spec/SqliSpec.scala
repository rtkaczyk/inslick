package com.accode.inslick.spec

import com.accode.inslick.data.{Animal, Queries}
import com.accode.inslick.db.{AnimalDao, DatabaseProvider}
import zio._
import zio.clock.Clock
import zio.test.Assertion.equalTo
import zio.test.TestAspect._
import zio.test._

abstract class SqliSpec extends DefaultRunnableSpec {
  def spec = suite(s"SqliSpec")(
    dbSuites: _*
  ) @@ parallel

  def dbSuites = Db.all.map(new DbSuite(_).dbSuite)

  class DbSuite(db: Db) {
    val provider = new DatabaseProvider(db.path)
    val queries  = new Queries(db.api)
    import provider._

    val tests = queries.all.map { q =>
      testM(q.name) {
        q.query.zio.map { r =>
          assert(r.headOption.getOrElse(0))(equalTo(q.excpectedN))
        }
      }
    }

    val initData: Task[Unit] = {
      import provider._
      (for {
        _ <- dao.drop
        _ <- dao.create
        _ <- dao.insertAll(Animal.all)
      } yield ()).zio
    }

    val dbSuite = suite(s"SqliInterpolator for ${db.path}")(
      tests: _*
    ) @@ sequential @@ beforeAll(initData)
  }

}
