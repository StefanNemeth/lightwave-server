import sbt._

object Dependencies {
  // Versions
  object Versions {
    val akka = "2.4.14"
    val scalaTest = "3.0.0"
  }

  // Libraries
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % Versions.akka
  val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % Versions.akka
  val akkaLog = "com.typesafe.akka" %% "akka-slf4j" % Versions.akka
  val akkaClusterSharding = "com.typesafe.akka" %% "akka-cluster-sharding" % Versions.akka
  val akkaDistributedData = "com.typesafe.akka" %% "akka-distributed-data-experimental" % Versions.akka
  val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % Versions.akka
  val logback = "ch.qos.logback" % "logback-classic" % "1.1.7"
  val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
  val scalactic = "org.scalactic" %% "scalactic" % Versions.scalaTest
  val scalaTest = "org.scalatest" %% "scalatest" % Versions.scalaTest % "test" exclude("org.scala-lang", "scala-reflect") exclude("org.scala-lang.modules", "scala-xml_2.11")
  val mockito = "org.mockito" % "mockito-core" % "2.2.28"
  val slick = "com.typesafe.slick" %% "slick" % "3.1.1"
  val postgreSQL = "postgresql" % "postgresql" % "9.1-901.jdbc4"
  val h2db = "com.h2database" % "h2" % "1.4.193"

  val commonDependencies = Seq(
    scalactic,
    scalaTest,
    mockito,
    logback,
    scalaLogging,
    slick,
    postgreSQL,
    h2db
  )

  val serviceDependencies = Seq(
    akkaActor,
    akkaCluster,
    akkaLog,
    akkaClusterSharding,
    akkaDistributedData,
    akkaTestKit
  )
}
