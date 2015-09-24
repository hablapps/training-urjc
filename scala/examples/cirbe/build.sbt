name := "cirbe"

version := "2.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.2.0-M3",
  "org.scalatest" % "scalatest_2.11" % "2.2.4", // % "test",
  "org.scala-lang" % "scala-reflect" % "2.11.7",
  "org.scalacheck" %% "scalacheck" % "1.12.5" % "test")

scalacOptions ++= Seq("-unchecked", "-deprecation")
