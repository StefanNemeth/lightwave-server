import Dependencies._

name    := "lightwave"
version := Commons.Versions.lightwave

scalaVersion in Global := Commons.Versions.scala

lazy val lightwaveCommon = Project(
  id = "lightwave-common",
  base = file("lightwave-common")).
  settings(Commons.settings: _*).
  settings(libraryDependencies ++= commonDependencies)

lazy val lightwaveConsole = Project(
  id = "lightwave-console",
  base = file("lightwave-console")).
  settings(Commons.settings: _*).
  settings(libraryDependencies ++= commonDependencies).
  settings(
    mainClass in Compile := Some("de.lightwave.console.ConsoleApp")
  ).
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
  dependsOn(lightwaveCommon)


val services = Seq(
  lightwaveRooms,
  lightwaveConsole
)