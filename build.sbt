name := "iCal4WS"
 
scalaVersion := "2.9.1"

seq(webSettings ++ Seq(
	port in container.Configuration := 9000
) : _*)

// using 0.2.4+ of the sbt web plugin
scanDirectories in Compile := Nil

resolvers ++= Seq(
  "Scala Tools Releases" at "http://scala-tools.org/repo-releases/",
  "Java.net Maven2 Repository" at "http://download.java.net/maven/2/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
  "Scala-Tools Maven2 Snapshots Repository" at "http://scala-tools.org/repo-releases/"
)

// if you have issues pulling dependencies from the scala-tools repositories (checksums don't match), you can disable checksums
//checksums := Nil

libraryDependencies ++= {
  val liftVersion = "2.4" // Put the current/latest lift version here
  Seq(
    "net.liftweb" %% "lift-webkit" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-mapper" % liftVersion % "compile->default",
    "net.liftweb" %% "lift-wizard" % liftVersion % "compile->default")
}

libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-webapp" % "8.0.4.v20111024" % "container", // For Jetty 8
  //"org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203" % "container", // For Jetty 7
  "org.specs2" %% "specs2" % "1.8.2" % "test", // For Specs2 tests
  "junit" % "junit" % "4.8" % "test->default", // For JUnit 4 testing
  "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
  "com.h2database" % "h2" % "1.2.138", // In-process database, useful for development systems
  "ch.qos.logback" % "logback-classic" % "0.9.26" % "compile->default", // Logging
  "org.mnode.ical4j" % "ical4j" % "1.0.3",
  "org.scala-tools.time" % "time_2.9.1" % "0.5",
  "com.typesafe.akka" % "akka-actor" % "2.0"
)
