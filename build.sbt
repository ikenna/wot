
name := "Ikenna's Simple Scala Project"

version := "1.0"

scalaVersion := "2.10.3"


resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

scalariformSettings

libraryDependencies ++= Seq(
"org.scalatest" % "scalatest_2.10" % "2.0" % "test",
"net.databinder" % "dispatch-http_2.10" % "0.8.10",
"ch.qos.logback" % "logback-classic" % "1.0.3",
"ch.qos.logback" % "logback-core" % "1.0.3" )


