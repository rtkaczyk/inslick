import sbt._

object Dependencies {
  lazy val dependencies = List(
    "com.typesafe.slick"   %% "slick"             % "3.0.3",
    "org.scalatest" %% "scalatest" % "3.0.5"
  )
}
