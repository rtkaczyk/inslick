package accode.inslick.spec
import accode.inslick.slickv.Slick
import accode.inslick.data.Queries
import zio.test.Assertion.equalTo
import zio.test.TestAspect._
import zio.test._

abstract class SqliSpec(slickF: Slick.Factory) extends DefaultRunnableSpec {
  def spec = suite(s"SqliSpec for Slick ${slickF.version}")(
    dbSuites: _*
  ) @@ parallel

  def dbSuites = DbDef.all.map(new DbSuite(_).dbSuite)

  class DbSuite(db: DbDef) {
    val queries = new Queries(db, slickF(db.path))
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
