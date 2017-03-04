package de.lightwave.rooms.engine.mapping

import org.scalatest.FunSuite

class VectorSpec extends FunSuite {
  test("Add two vectors") {
    assert((Vector2(1, 1) + Vector2(2, 2)) === Vector2(3, 3))
    assert((Vector3(1, 1, 1) + Vector3(2, 2, 2)) === Vector3(3, 3, 3))
  }

  test("Subtract two vectors") {
    assert((Vector2(2, 2) - Vector2(1, 1)) === Vector2(1, 1))
    assert((Vector3(2, 2, 2) - Vector3(1, 1, 1)) === Vector3(1, 1, 1))
  }

  test("Get length of vector") {
    assert(Vector2(2, 2).length === 2)
    assert(Vector3(2, 2, 2).length === 3)
  }

  test("Get vector from parsable string") {
    assert(Vector2.from("1;1") === Vector2(1, 1))
    assert(Vector3.from("1;1;1") === Vector3(1, 1, 1))
  }

  test("Get empty vector from unparsable string") {
    assert(Vector2.from("abc") === new Vector2(0, 0))
    assert(Vector3.from("abc") === new Vector3(0, 0, 0))
  }

  test("Get movement direction") {
    assert(RoomDirection.getMovementDirection(Vector2(1), Vector2(2)).contains(RoomDirection.South))
    assert(RoomDirection.getMovementDirection(Vector2(2), Vector2(1)).contains(RoomDirection.North))
    assert(RoomDirection.getMovementDirection(Vector2(0, 1), Vector2(0, 2)).contains(RoomDirection.West))
    assert(RoomDirection.getMovementDirection(Vector2(0, 2), Vector2(0, 1)).contains(RoomDirection.East))
    assert(RoomDirection.getMovementDirection(Vector2.empty, Vector2(1, 1)).contains(RoomDirection.SouthWest))
    assert(RoomDirection.getMovementDirection(Vector2(1, 1), Vector2.empty).contains(RoomDirection.NorthEast))
    assert(RoomDirection.getMovementDirection(Vector2(1, 3), Vector2(2, 1)).contains(RoomDirection.SouthEast))
    assert(RoomDirection.getMovementDirection(Vector2(2, 1), Vector2(1, 3)).contains(RoomDirection.NorthWest))
    assert(RoomDirection.getMovementDirection(Vector2.empty, Vector2.empty).isEmpty)
  }
}
