package accode.inslick.spec
import accode.inslick.slickv.{AnimalDao, PersonDao, Slick33}
import zio.Task
import zio.test.TestAspect._
import zio.test._

object InitTablesSpec extends DefaultRunnableSpec {

  def spec = suite("Initialise tables")(
    tests: _*
  ) @@ parallel

  def tests = DbDef.all.map { db =>
    testM(s"${db.path}") {
      initData(Slick33(db.path))
    }
  }

  def initData(slick33: Slick33): Task[TestResult] = {
    import slick33._
    val animal = new AnimalDao(slick33)
    val person = new PersonDao(slick33)

    val run = for {
      _ <- animal.drop
      _ <- animal.create
      _ <- person.drop
      _ <- person.create
    } yield assertCompletes

    run.zio
  }
}
