import Dependencies._

name    := "lightwave"
version := Commons.Versions.lightwave

scalaVersion in Global := Commons.Versions.scala

lazy val lightwaveCommon = Project(
  id = "lightwave-common",
  base = file("lightwave-common")).
  settings(Commons.settings: _*).
  settings(libraryDependencies ++= commonDependencies)

lazy val lightwaveService = Project(
  id = "lightwave-service",
  base = file("lightwave-service")).
  settings(Commons.settings: _*).
  settings(libraryDependencies ++= serviceDependencies).
  dependsOn(lightwaveCommon)

lazy val lightwaveRooms = Project(
  id = "lightwave-rooms",
  base = file("lightwave-rooms")).
  enablePlugins(JavaAppPackaging).
  settings(Commons.settings: _*).
  settings(libraryDependencies ++= commonDependencies).
  settings(
    mainClass in Compile := Some("de.lightwave.rooms.RoomServiceApp")
  ).
  dependsOn(lightwaveService)


val services = Seq(
  lightwaveRooms
)