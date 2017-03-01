import sbt._
import Keys.{mainClass, _}
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.docker.DockerPlugin

object Commons {
  object Versions {
    val lightwave = "0.1-SNAPSHOT"
    val scala = "2.11.8"
  }

  val globalResources: sbt.File = file("resources")

  val settings = Seq(
    version := Versions.lightwave,
    scalaVersion := Versions.scala,
    resolvers += Opts.resolver.mavenLocalFile,
    unmanagedResourceDirectories in Compile += globalResources
  )

  def module(id: String): sbt.Project = Project(
    id = "lightwave-" + id,
    base = file("lightwave-" + id)).
    settings(Commons.settings: _*).
    settings(libraryDependencies ++= Dependencies.commonDependencies)

  def service(id: String, mainCName: String): sbt.Project = module(id).
    enablePlugins(DockerPlugin, JavaAppPackaging).
    settings(
      mainClass in Compile := Some(mainCName)
    )
}