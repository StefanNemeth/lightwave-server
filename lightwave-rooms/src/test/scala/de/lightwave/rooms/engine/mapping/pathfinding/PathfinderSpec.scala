package de.lightwave.rooms.engine.mapping.pathfinding

import de.lightwave.rooms.engine.mapping.Vector2
import org.scalatest.FunSuite

class PathfinderSpec extends FunSuite {
  test("Get distance from one vector to another") {
    assert(Pathfinder.calculateDistance(Vector2(0, 0), Vector2(1, 1)) == 1)
  }
}
