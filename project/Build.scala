import sbt._
import Keys._

object MinimalBuild extends Build {
  
  lazy val buildVersion =  "2.0-RC3"
  
  lazy val typesafe = "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
  lazy val typesafeSnapshot = "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/"

  lazy val root = Project(id = "iCal", base = file("."), settings = Project.defaultSettings).settings(
    version := buildVersion,
    organization := "eu.kaatz",
    resolvers += typesafe,
    resolvers += typesafeSnapshot,
    libraryDependencies += "com.typesafe" %% "play-mini" % buildVersion withSources() withJavadoc(),
    libraryDependencies += "com.github.jsuereth.scala-arm" %% "scala-arm" % "1.0" withSources() withJavadoc(),
    libraryDependencies += "org.mnode.ical4j" % "ical4j" % "1.0.3" withSources() withJavadoc(),
    mainClass in (Compile, run) := Some("play.core.server.NettyServer")
  )
}
