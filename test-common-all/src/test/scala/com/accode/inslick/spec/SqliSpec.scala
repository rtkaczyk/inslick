package com.accode.inslick.spec

import com.accode.inslick.data.{Animal, Queries}
import com.accode.inslick.db.{AnimalDao, DatabaseProvider}
import zio._
import zio.clock.Clock
import zio.test.Assertion.equalTo
import zio.test.TestAspect._
import zio.test._

abstract class SqliSpec(dbProviderFactory: DatabaseProvider.Factory) extends DefaultRunnableSpec {
  def spec = suite(s"SqliSpec")(
    dbSuites: _*
  ) @@ parallel

  def dbSuites = DbDef.all.map(new DbSuite(_).dbSuite)

  class DbSuite(db: DbDef) {
    val provider = dbProviderFactory(db.path)
    val queries  = new Queries(db.api, provider)
    import provider._

    val tests = queries.all.map { q =>
      testM(q.name) {
        q.query.zio.map { r =>
          assert(r)(equalTo(q.excpectedN))
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
