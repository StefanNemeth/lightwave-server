package de.lightwave.rooms.engine.mapping.pathfinding
import de.lightwave.rooms.engine.mapping.{MapUnit, RoomMap, Vector2}

object TestPathfinder extends Pathfinder {
  override def calculateNextStep(currentPosition: Vector2, destination: Vector2)(implicit states: RoomMap[MapUnit]): Option[Vector2] = Some(Vector2(1))
}
