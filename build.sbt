name := "ScalaRSS"

version := "0.1"

scalaVersion := "2.13.0"

resolvers += "jitpack" at "https://jitpack.io"
resolvers += "dl-john-ky" at "http://dl.john-ky.io/maven/releases"

libraryDependencies ++= Seq(
  "net.debasishg" %% "redisclient" % "3.10",
  "com.typesafe.play" %% "play-json" % "2.8.0-M4",
  "org.foundationdb" % "fdb-java" % "6.1.9",
  "org.scalaj" %% "scalaj-http" % "2.4.2",
  "org.scala-lang.modules" %% "scala-xml" % "1.2.0",
  "com.github.xaanit" % "Scalalin" % "1.1"
)

mainClass in assembly := Some("it.xaan.rss.Main")
assemblyJarName in assembly := "RSS.jar"
assemblyMergeStrategy in assembly := {
  case PathList("org", "eclipse", "jetty", "http", "encoding.properties") => MergeStrategy.first
  case a if a.contains("netty") => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
