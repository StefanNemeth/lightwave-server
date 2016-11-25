import sbt._

object Dependencies {
  // Versions
  val akkaVersion = "2.4.14"

  // Libraries
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % akkaVersion
  val akkaLog = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
  val logback = "ch.qos.logback" % "logback-classic" % "1.1.7"
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"

  val commonDependencies = Seq(
    akkaActor, akkaCluster, akkaLog, logback, scalaLogging
  )
}
