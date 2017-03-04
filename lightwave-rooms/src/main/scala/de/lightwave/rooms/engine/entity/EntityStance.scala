package de.lightwave.rooms.engine.entity

import de.lightwave.rooms.engine.mapping.{RoomDirection, Vector3}

case class EntityStance(properties: Set[StanceProperty], headDirection: RoomDirection, bodyDirection: RoomDirection)

sealed trait StanceProperty

object StanceProperty {
  case class WalkingTo(pos: Vector3) extends StanceProperty

  implicit class PropertyFilterSupport(properties: Set[StanceProperty]) {
    def without[T]: Set[StanceProperty] = properties.filter(!_.isInstanceOf[T])
    def replace[S<:StanceProperty](w: S): Set[StanceProperty] = without[S] + w
  }
}