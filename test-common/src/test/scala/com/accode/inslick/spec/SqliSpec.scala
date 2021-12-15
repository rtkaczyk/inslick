package com.accode.inslick.spec

import com.accode.inslick.data.Queries
import com.accode.inslick.slick.SqlRunner
import zio.test.Assertion.equalTo
import zio.test.TestAspect._
import zio.test._

abstract class SqliSpec(sqlRunnerFactory: SqlRunner.Factory) extends DefaultRunnableSpec {
  def spec = suite(s"SqliSpec for Slick ${sqlRunnerFactory.version}")(
    dbSuites: _*
  ) @@ parallel

  def dbSuites = DbDef.all.map(new DbSuite(_).dbSuite)

  class DbSuite(db: DbDef) {
    val queries = new Queries(db, sqlRunnerFactory(db.path))
    val tests = queries.all.map { q =>
      testM(q.name) {
        q.query.map { r =>
          assert(r)(equalTo(q.expected))
        }
      }
    }
    val dbSuite = suite(db.path)(
      tests: _*
    ) @@ sequential
  }
}
