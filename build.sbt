import Dependencies._

ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.accode"
ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
ThisBuild / scalacOptions ++= List("-feature", "-deprecation", "-unchecked")

lazy val root = project
  .in(file("."))
  .settings(
    name                := "root",
    publish / aggregate := false,
    scalacOptions ++= List("-feature", "-deprecation", "-unchecked")
  )
  .aggregate(inslick, tests)

lazy val inslick = project
  .in(file("inslick"))
  .settings(
    scalaVersion        := scala211,
    crossScalaVersions  := scalaVersions,
    libraryDependencies := List(scalaReflect(scalaVersion.value), slick % Provided)
  )

lazy val tests = project
  .in(file("tests"))
  .settings(
    scalaVersion       := scala211,
    crossScalaVersions := scalaVersions,
    libraryDependencies := List(
      slick,
      h2,
      postgres,
      mysql,
      sqlite,
      logback,
      zioTest,
      zioTestSbt
    )
  )
  .dependsOn(inslick)

// Uncomment the following for publishing to Sonatype.
// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for more detail.

// ThisBuild / description := "Some descripiton about your project."
// ThisBuild / licenses    := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
// ThisBuild / homepage    := Some(url("https://github.com/example/project"))
// ThisBuild / scmInfo := Some(
//   ScmInfo(
//     url("https://github.com/your-account/your-project"),
//     "scm:git@github.com:your-account/your-project.git"
//   )
// )
// ThisBuild / developers := List(
//   Developer(
//     id    = "Your identifier",
//     name  = "Your Name",
//     email = "your@email",
//     url   = url("http://your.url")
//   )
// )
// ThisBuild / pomIncludeRepository := { _ => false }
// ThisBuild / publishTo := {
//   val nexus = "https://oss.sonatype.org/"
//   if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
//   else Some("releases" at nexus + "service/local/staging/deploy/maven2")
// }
// ThisBuild / publishMavenStyle := true
