package de.lightwave.rooms.engine.mapping.pathfinding

import de.lightwave.rooms.engine.mapping.{MapUnit, RoomDirection, RoomMap, Vector2}

trait Pathfinder {
  def findNextStep(currentPosition: Vector2, destination: Vector2)(implicit states: RoomMap[MapUnit]): Option[Vector2]
}

object Pathfinder {
  def calculateDistance(from: Vector2, to: Vector2): Int =
    Math.sqrt(Math.pow(Math.abs(to.x - from.x), 2) + Math.pow(Math.abs(to.y - from.y), 2)).toInt

  def getPotentialNeighbours(of: Vector2): Seq[Vector2] = Seq(
    RoomDirection.South, RoomDirection.SouthWest, RoomDirection.SouthEast,
    RoomDirection.West, RoomDirection.East, RoomDirection.North,
    RoomDirection.NorthWest, RoomDirection.NorthEast).map(d => of + d)

  case class Node(pos: Vector2, previous: Option[Node] = None)
}

case class PathNode(pos: Vector2, previousNode: Option[PathNode] = None, f: Int = 0, g: Int = 0)