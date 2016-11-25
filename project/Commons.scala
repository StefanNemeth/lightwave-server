import sbt._
import Keys._

object Commons {
  object Versions {
    val lightwave = "0.0.1"
    val scala = "2.11.7"
  }

  val settings: Seq[Def.Setting[_]] = Seq(
    version := Versions.lightwave,
    scalaVersion := Versions.scala,
    resolvers += Opts.resolver.mavenLocalFile
  )
}
