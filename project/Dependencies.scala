import sbt._

object Dependencies {

  val scala213      = "2.13.7"
  val scala212      = "2.12.15"
  val scala211      = "2.11.12"
  val scalaVersions = List(scala213, scala212, scala211)

  val slick = "com.typesafe.slick" %% "slick" % "3.3.3"

  //  def oldSchool: Unit = {
  //    val ps = dbConfig.db.createSession().prepareStatement(
  //      "select count(*) from animal a where (a.has_tail, a.id) in (values (?, ?), (?, ?))"
  //    )
  //    Animal.all.take(2).zipWithIndex.foreach { case (a, i) =>
  //      ps.setBoolean(2 * i + 1, a.hasTail)
  //      ps.setInt(2 * i + 2, a.id)
  //    }
  //    val res  = ps.executeQuery()
  //    var rows = 0
  //    while (res.next()) { rows += 1 }
  //    println(s"PREPARED STATEMENT RESULTS: $rows")
  //    ps.closeOnCompletion()
  //  }
  // val h2       = "com.h2database" % "h2"         % "2.0.202" % Test
  val h2       = "com.h2database" % "h2"         % "1.4.200" % Test
  val postgres = "org.postgresql" % "postgresql" % "42.3.1"  % Test

  val logback = "ch.qos.logback" % "logback-classic" % "1.2.7" % Test

  val zioTest    = "dev.zio" %% "zio-test"     % "1.0.12" % Test
  val zioTestSbt = "dev.zio" %% "zio-test-sbt" % "1.0.12" % Test

  def scalaReflect(version: String) = "org.scala-lang" % "scala-reflect" % version
}
