name := "Akka-Example"
 
version := "1.0"
 
scalaVersion := "2.11.5"
 
resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

val akkaVersion = "2.3.9"
 
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
)
