import Dependencies._

name    := "lightwave"
version := Commons.Versions.lightwave

scalaVersion in Global := Commons.Versions.scala

lazy val common = Commons.module("common")

lazy val players = Commons.service("players", "de.lightwave.players.PlayerServiceApp")
  .dependsOn(common)

lazy val rooms = Commons.service("rooms", "de.lightwave.rooms.RoomServiceApp")
  .dependsOn(common)

lazy val shockwave = Commons.service("shockwave", "de.lightwave.shockwave.ShockwaveServiceApp")
  .dependsOn(common, rooms, players)

val services = Seq(
  rooms,
  players,
  shockwave
)