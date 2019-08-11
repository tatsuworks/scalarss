name := "ScalaRSS"

version := "0.1"

scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
  "net.debasishg" %% "redisclient" % "3.10",
  "com.typesafe.play" %% "play-json" % "2.8.0-M4",
  "com.softwaremill.sttp" %% "core" % "1.6.4",
  "com.rometools" % "rome" % "1.12.1",
  "org.foundationdb" % "fdb-java" % "6.1.9"
)