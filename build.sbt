import Dependencies._

ThisBuild / version      := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.accode"

ThisBuild / crossScalaVersions := scalaVersions
ThisBuild / scalacOptions ++= List("-feature", "-deprecation", "-unchecked")
ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

lazy val root = project
  .in(file("."))
  .settings(
    name                       := "root",
    scalaVersion               := scala211,
    publish / aggregate        := false,
    Global / parallelExecution := false
  )
  .aggregate(inslick, testCommonAll, testCommon31, testCommon33, test33, test32, test31, test30)

lazy val inslick = project
  .in(file("inslick"))
  .settings(
    libraryDependencies := List(scalaReflect(scalaVersion.value), slick33 % Provided)
  )

lazy val testCommonAll = project
  .in(file("test-common-all"))
  .settings(
    libraryDependencies := List(
      slick33 % Provided,
      h2,
      postgres,
      mysql,
      sqlite,
      logback,
      sourceCode,
      zioTest,
      zioTestSbt
    )
  )
  .dependsOn(inslick)

lazy val testCommon33 = project
  .in(file("test-common-33"))
  .settings(
    libraryDependencies ++= List(slick33 % Provided)
  )
  .dependsOn(testCommonAll % "test->test")

lazy val testCommon31 = project
  .in(file("test-common-31"))
  .settings(
    crossScalaVersions := List(scala211),
    libraryDependencies ++= List(slick31 % Provided)
  )
  .dependsOn(testCommonAll % "test->test")

lazy val test33 = project
  .in(file("test-33"))
  .settings(
    libraryDependencies += slick33
  )
  .dependsOn(testCommon33 % "test->test")

lazy val test32 = project
  .in(file("test-32"))
  .settings(
    crossScalaVersions := List(scala211, scala212),
    libraryDependencies += slick32
  )
  .dependsOn(testCommon33 % "test->test")

lazy val test31 = project
  .in(file("test-31"))
  .settings(
    crossScalaVersions := List(scala211),
    libraryDependencies += slick31
  )
  .dependsOn(testCommon31 % "test->test")

lazy val test30 = project
  .in(file("test-30"))
  .settings(
    crossScalaVersions := List(scala211),
    libraryDependencies += slick30
  )
  .dependsOn(testCommon31 % "test->test")

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
