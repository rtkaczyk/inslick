import sbt._

object Dependencies {

  val scala213      = "2.13.7"
  val scala212      = "2.12.15"
  val scala211      = "2.11.12"
  val scalaVersions = List(scala213, scala212, scala211)

  val slick33 = "com.typesafe.slick" %% "slick" % "3.3.3"
  val slick32 = "com.typesafe.slick" %% "slick" % "3.2.3"
  val slick31 = "com.typesafe.slick" %% "slick" % "3.1.1"
  val slick30 = "com.typesafe.slick" %% "slick" % "3.0.3"

  val h2       = "com.h2database" % "h2"                   % "2.0.202"  % Test
  val postgres = "org.postgresql" % "postgresql"           % "42.3.1"   % Test
  val mysql    = "mysql"          % "mysql-connector-java" % "8.0.25"   % Test
  val sqlite   = "org.xerial"     % "sqlite-jdbc"          % "3.36.0.2" % Test

  val logback    = "ch.qos.logback" % "logback-classic" % "1.2.7"  % Test
  val sourceCode = "com.lihaoyi"   %% "sourcecode"      % "0.2.7"  % Test
  val zioTest    = "dev.zio"       %% "zio-test"        % "1.0.12" % Test
  val zioTestSbt = "dev.zio"       %% "zio-test-sbt"    % "1.0.12" % Test

  def scalaReflect(version: String) = "org.scala-lang" % "scala-reflect" % version
}
