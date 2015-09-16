name := "cirbe"

version := "2.0"

scalaVersion := "2.11.7"

libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.0-M3"

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.11.7"

scalacOptions ++= Seq("-unchecked", "-deprecation")
