package de.lightwave.migration

import de.lightwave.rooms.engine.entity.StanceProperty.WalkingTo
import de.lightwave.rooms.engine.mapping.Vector3
import org.scalatest.FunSuite

class ShockwaveMigrationSpec extends FunSuite {
  test("Convert map object to serialized shockwave map") {
    assert(ShockwaveMigration.composeMap(IndexedSeq(IndexedSeq(Some(0), None, Some(1)), IndexedSeq(Some(1), Some(0), None))) == "01\rx0\r1x")
  }

  test("Compose entity statuses") {
    assert(ShockwaveMigration.composeEntityStatuses(Set(WalkingTo(Vector3.empty))) == "mv 0,0,0.0")
  }
}