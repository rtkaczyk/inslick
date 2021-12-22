import Dependencies._

ThisBuild / version := "1.0.0"

ThisBuild / scalaVersion := scala211
ThisBuild / scalacOptions ++= List("-feature", "-deprecation", "-unchecked")
ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

lazy val publishSettings = List(
  organization      := "io.github.rtkaczyk",
  description       := "Plain SQL interpolation extension for Slick 3.x",
  licenses          := List("MIT" -> url("https://opensource.org/licenses/MIT")),
  homepage          := Some(url("https://github.com/rtkaczyk/inslick")),
  scmInfo           := Some(ScmInfo(homepage.value.get, "git@github.com:rtkaczyk/inslick.git")),
  publishMavenStyle := true,
  publishTo         := sonatypePublishToBundle.value,
  sonatypeCredentialHost := "s01.oss.sonatype.org",
  developers := List(
    Developer(
      id = "rtkaczyk",
      name = "Radek Tkaczyk",
      email = "rtkaczyk.github@gmail.com",
      url = url("https://github.com/rtkaczyk")
    )
  )
)

lazy val root = project
  .in(file("."))
  .settings(
    name                       := "root",
    scalaVersion               := scala211,
    publish / aggregate        := false,
    Global / parallelExecution := false
  )
  .aggregate(inslick, testCommon, test33, test32, test31, test30)

lazy val inslick = project
  .in(file("inslick"))
  .settings(
    crossScalaVersions  := List(scala211, scala212, scala213),
    libraryDependencies := List(scalaReflect(scalaVersion.value), slick33 % Provided)
  )
  .settings(publishSettings: _*)

lazy val testCommon = project
  .in(file("test-common"))
  .settings(
    crossScalaVersions := List(scala211, scala212, scala213),
    libraryDependencies := List(
      slick33,
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

lazy val test33 = project
  .in(file("test-33"))
  .settings(
    crossScalaVersions := List(scala211, scala212, scala213),
    libraryDependencies += slick33
  )
  .dependsOn(testCommon % "test->test")

lazy val test32 = project
  .in(file("test-32"))
  .settings(
    crossScalaVersions := List(scala211, scala212),
    libraryDependencies += slick32,
    dependencyOverrides := List(slick32)
  )
  .dependsOn(testCommon % "test->test")

lazy val test31 = project
  .in(file("test-31"))
  .settings(
    crossScalaVersions := List(scala211),
    libraryDependencies += slick31,
    dependencyOverrides := List(slick31)
  )
  .dependsOn(testCommon % "test->test")

lazy val test30 = project
  .in(file("test-30"))
  .settings(
    crossScalaVersions := List(scala211),
    libraryDependencies += slick30,
    dependencyOverrides := List(slick30)
  )
  .dependsOn(testCommon % "test->test")
