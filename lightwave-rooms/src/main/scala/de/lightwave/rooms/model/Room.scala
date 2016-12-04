package de.lightwave.rooms.model

import de.lightwave.rooms.model.Rooms.RoomId

case class Room(id: Option[RoomId], name: String, description: String)

object Rooms {
  type RoomId = Int
}