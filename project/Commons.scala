import sbt._
import Keys._

object Commons {
  object Versions {
    val lightwave = "0.1-SNAPSHOT"
    val scala = "2.11.8"
  }

  val globalResources = file("resources")

  val settings = Seq(
    version := Versions.lightwave,
    scalaVersion := Versions.scala,
    resolvers += Opts.resolver.mavenLocalFile,
    unmanagedResourceDirectories in Compile += globalResources
  )
}