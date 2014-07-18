
name := "Word of Tweet"

version := "1.0"

scalaVersion := "2.11.1"


resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

scalariformSettings

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.4",
  "com.typesafe.akka" %% "akka-actor" % "2.3.4",
  "org.scalatest" %% "scalatest" % "2.2.0" % "test",
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.1",
"ch.qos.logback" % "logback-classic" % "1.0.3",
  "ch.qos.logback" % "logback-core" % "1.0.3",
  "org.jsoup" % "jsoup" % "1.7.3",
  "org.seleniumhq.selenium" % "selenium-java" % "2.42.2",
  "mysql" % "mysql-connector-java" % "5.1.31",
  "org.springframework" % "spring-jdbc" % "4.0.5.RELEASE",
  "com.h2database" % "h2" % "1.4.179",
  "org.twitter4j" % "twitter4j-core" % "4.0.2",
  "org.json4s" %% "json4s-native" % "3.2.10",
  "org.json4s" %% "json4s-jackson" % "3.2.10",
  "com.github.tototoshi" %% "scala-csv" % "1.0.0"
)


