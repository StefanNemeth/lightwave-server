package de.lightwave.rooms.engine.entities

import de.lightwave.rooms.engine.mapping.{RoomDirection, Vector3}

case class EntityStance(properties: Seq[StanceProperty], headDirection: RoomDirection, bodyDirection: RoomDirection)

sealed trait StanceProperty

object StanceProperty {
  case class WalkingTo(pos: Vector3) extends StanceProperty
}