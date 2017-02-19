package de.lightwave.migration

import de.lightwave.rooms.engine.mapping.RoomMap.StaticMap
import org.scalatest.FunSuite

class ShockwaveMigrationSpec extends FunSuite {
  test("Convert map object to serialized shockwave map") {
    assert(ShockwaveMigration.composeMap(IndexedSeq(IndexedSeq(Some(0), None, Some(1)), IndexedSeq(Some(1), Some(0), None))) == "01\rx0\r1x")
  }
}