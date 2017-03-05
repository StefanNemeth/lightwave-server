package de.lightwave.rooms.engine.mapping.pathfinding

import de.lightwave.rooms.engine.mapping.{MapUnit, RoomMap, Vector2}

/**
  * Probably doesn't find the entire path, but it helps the entity
  * to get near the destination.
  */
object SimplePathfinder extends Pathfinder {
  def findNextStep(currentPosition: Vector2, destination: Vector2)(implicit states: RoomMap[MapUnit]): Option[Vector2] = {
    var x = 0
    var y = 0

    if (currentPosition.x < destination.x) {
      x = 1
    } else if (currentPosition.x > destination.x) {
      x = -1
    }

    if (currentPosition.y < destination.y) {
      y = 1
    } else if (currentPosition.y > destination.y) {
      y = -1
    }

    val potentialTarget = currentPosition + Vector2(x, y)

    states.get(potentialTarget.x, potentialTarget.y) match {
      case Some(MapUnit.Tile.Clear) => Some(potentialTarget)
      case _ => None
    }
  }
}
