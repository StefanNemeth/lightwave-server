import Dependencies._

name := "lightwave"

// Service base
lazy val lightwaveService = Project(
  id = "lightwave-service",
  base = file("lightwave-service")
).settings(Commons.settings: _*).settings(
  libraryDependencies ++= commonDependencies
)
