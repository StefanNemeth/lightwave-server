package de.lightwave.rooms.engine.mapping.pathfinding

import de.lightwave.rooms.engine.mapping.{RoomDirection, Vector2}

trait Pathfinder {
  def calculateNextStep(currentPosition: Vector2, destination: Vector2): Option[Vector2]
}

/**
  * Some functions that help implementing a pathfinder
  */
object Pathfinder {
  def calculateDistance(from: Vector2, to: Vector2): Int =
    Math.sqrt(Math.pow(to.x - from.x, 2) + Math.pow(to.y - from.y, 2)).toInt

  def getPotentialNeighbours(of: Vector2): Seq[Vector2] = Seq(
    RoomDirection.South, RoomDirection.SouthWest, RoomDirection.SouthEast,
    RoomDirection.West, RoomDirection.East, RoomDirection.North,
    RoomDirection.NorthWest, RoomDirection.NorthEast).map(d => of + d)

  case class Node(pos: Vector2, previous: Option[Node] = None)
}

object DumbPathfinder extends Pathfinder {
  def calculateNextStep(currentPosition: Vector2, destination: Vector2): Option[Vector2] = {
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

    Some(currentPosition + Vector2(x, y))
  }
}