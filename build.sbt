import Dependencies._

name    := "lightwave"
version := Commons.Versions.lightwave

scalaVersion in Global := Commons.Versions.scala

lazy val lightwaveCommon = Project(
  id = "lightwave-common",
  base = file("lightwave-common")).
  settings(Commons.settings: _*).
  settings(libraryDependencies ++= commonDependencies)

lazy val lightwavePlayers = Project(
  id = "lightwave-players",
  base = file("lightwave-players")).
  enablePlugins(JavaAppPackaging).
  settings(Commons.settings: _*).
  settings(libraryDependencies ++= commonDependencies).
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

lazy val lightwaveShockwave = Project(
  id = "lightwave-shockwave",
  base = file("lightwave-shockwave")).
  settings(Commons.settings: _*).
  settings(libraryDependencies ++= commonDependencies).
  settings(
    mainClass in Compile := Some("de.lightwave.shockwave.ShockwaveServiceApp")
  ).
  dependsOn(lightwaveCommon).
  dependsOn(lightwaveRooms).
  dependsOn(lightwavePlayers)

val services = Seq(
  lightwaveRooms,
  lightwaveShockwave
)