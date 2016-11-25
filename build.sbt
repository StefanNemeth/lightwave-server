import Dependencies._

name := "lightwave"

// Service base
lazy val lightwaveService = Project(
  id = "lightwave-service",
  base = file("lightwave-service")
).settings(Commons.settings: _*).settings(
  libraryDependencies ++= commonDependencies
)

// Room service
lazy val lightwaveRooms = Project(
  id = "lightwave-rooms",
  base = file("lightwave-rooms")
).settings(Commons.settings: _*).dependsOn(lightwaveService)