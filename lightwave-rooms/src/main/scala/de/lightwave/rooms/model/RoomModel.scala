package de.lightwave.rooms.model

import de.lightwave.rooms.model.RoomModels.RoomModelId

case class RoomModel(id: Option[RoomModelId], heightMap: String, doorPosition: String)

object RoomModels {
  type RoomModelId = String

  val DefaultMap: RoomModel = RoomModel(None, "0000\n0000\n0000\n0000", "0;0")
}
