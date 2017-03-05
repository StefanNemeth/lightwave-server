package de.lightwave.rooms.engine.mapping.pathfinding

import de.lightwave.rooms.engine.mapping.{MapUnit, RoomMap, Vector2}
import org.scalatest.FunSuite

class AStarPathfinderSpec extends FunSuite {
  implicit val states = new RoomMap[MapUnit](10, 10)

  // Initialize test map
  for {
    x <- 0 until 10
    y <- 0 until 10
  } states.set(x, y)(MapUnit.Tile.Clear)

  test("Get next step to destination") {
    println(AStarPathfinder.findNextStep(Vector2.empty, Vector2(0, 5)))
  }
}
