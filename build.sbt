name := "ashnimda"

version := "0.0.1"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

scalaVersion := "2.11.8"

scalacOptions in Test ++= Seq("-Yrangepos")

val akkaVersion = "2.4.9"

mainClass in Compile := Some("ashnimda.Main")

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-actor" % akkaVersion,
	"com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-experimental" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental" % akkaVersion,
	"org.scala-lang.modules" %% "scala-xml" % "1.0.5",
	"org.scalaz" %% "scalaz-core" % "7.1.3",
	"org.specs2" %% "specs2-core" % "3.6.5" % "test",
	"org.specs2" %% "specs2-junit" % "3.6.5" % "test",
	"com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",	
	"org.scalatest" %% "scalatest" % "2.2.4" % "test",
	"junit" % "junit" % "4.8.1" % "test"
)