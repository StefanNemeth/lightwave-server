package de.lightwave.rooms.engine.mapping.pathfinding

import de.lightwave.rooms.engine.mapping.{RoomDirection, Vector2}

trait Pathfinder {
  def calculateNextStep(currentPosition: Vector2, destination: Vector2): Option[Vector2]
}

object Pathfinder {
  def getPotentialNeighbours(of: Vector2): Seq[Vector2] = Seq(
    of.to(RoomDirection.South),
    of.to(RoomDirection.SouthWest),
    of.to(RoomDirection.SouthEast),
    of.to(RoomDirection.West),
    of.to(RoomDirection.East),
    of.to(RoomDirection.North),
    of.to(RoomDirection.NorthWest),
    of.to(RoomDirection.NorthEast)
  )
}

case class Node(pos: Vector2, previous: Option[Node] = None)