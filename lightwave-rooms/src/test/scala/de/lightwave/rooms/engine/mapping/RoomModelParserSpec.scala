package de.lightwave.rooms.engine.mapping

import de.lightwave.rooms.model.RoomModel
import org.scalatest.FunSuite

class RoomModelParserSpec extends FunSuite {
  test("Throw exception on parsing invalid map string to empty room map") {
    intercept[IllegalArgumentException] {
      // 0 rows
      RoomModelParser.toEmptyMap("")
    }

    intercept[IllegalArgumentException] {
      // 0 columns
      RoomModelParser.toEmptyMap("\n")
    }
  }

  test("Parse valid map string to empty room map") {
    assert(RoomModelParser.toEmptyMap("x\nx").cols === 2)
    assert(RoomModelParser.toEmptyMap("xxx").rows === 3)
  }

  test("Parse valid map string to height map") {
    val map = RoomModelParser.toHeightMap(RoomModel(None, "1x\nxx\n12", ""))

    assert(map.get(0, 0) === Some(1))
    assert(map.get(0, 1) === None)
    assert(map.get(1, 2) === Some(2))
  }

  test("Parse valid map string to state map") {
    val map = RoomModelParser.toStateMap(RoomModel(None, "x10", "2;0"))

    assert(map.get(0, 0) === Some(Void))
    assert(map.get(1, 0) === Some(Tile))
    assert(map.get(2, 0) === Some(Door))
  }
}
