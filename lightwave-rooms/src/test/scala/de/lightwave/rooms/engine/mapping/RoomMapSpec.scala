package de.lightwave.rooms.engine.mapping

import org.scalatest.FunSuite

class RoomMapSpec extends FunSuite {
  test("Throw exception on invalid map size") {
    intercept[IllegalArgumentException] {
      new RoomMap[Int](-1, -1)
    }
  }

  test("Check if tile exists") {
    val map = new RoomMap[Int](1, 1)

    assert(map.exists(0, 0) === true)
    assert(map.exists(1, 1) === false)
  }

  test("Set tile of map") {
    val map = new RoomMap[Int](1, 1)

    map.set(0, 0)(1)
  }

  test("Get tile of map") {
    val map = new RoomMap[Int](2, 1)

    map.set(0, 0)(1)

    assert(map.get(0, 0) === Some(1))
    assert(map.get(1, 0) === None)
    assert(map.get(2, 2) === None)
  }

  test("Convert room map to static map") {
    val map = new RoomMap[Int](1, 1)

    map.set(0, 0)(1)
    assert(map.toStatic == IndexedSeq(IndexedSeq(Some(1))))
  }
}
