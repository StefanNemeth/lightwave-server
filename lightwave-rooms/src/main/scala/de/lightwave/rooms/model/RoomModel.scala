package de.lightwave.rooms.model

import de.lightwave.rooms.model.RoomModels.RoomModelId

case class RoomModel(id: Option[RoomModelId], heightmap: String)

object RoomModels {
  type RoomModelId = String
}
