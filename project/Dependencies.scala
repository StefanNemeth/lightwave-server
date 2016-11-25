import sbt._

object Dependencies {
  // Versions
  val akkaVersion = "2.4.14"

  // Libraries
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % akkaVersion

  val commonDependencies: Seq[ModuleID] = Seq(akkaActor, akkaCluster)
}
