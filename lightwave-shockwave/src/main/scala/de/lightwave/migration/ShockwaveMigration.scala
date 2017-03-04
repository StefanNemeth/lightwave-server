package de.lightwave.migration

import de.lightwave.rooms.engine.entity.StanceProperty
import de.lightwave.rooms.engine.entity.StanceProperty.WalkingTo
import de.lightwave.rooms.engine.mapping.{RoomDirection, Vector3}
import de.lightwave.rooms.engine.mapping.RoomMap.StaticMap

/**
  * Helper functions for migrating stuff to
  * Shockwave
  */
object ShockwaveMigration {
  /**
    * Convert a height map object to a string that is
    * readable by a Shockwave client
    */
  def composeMap(map: StaticMap[Double]): String = if (map.size < 1) "" else {
    val mapBuilder = new StringBuilder()
    for (y <- map(0).indices) {
      for (x <- map.indices) mapBuilder.append(map(x)(y) match {
        case Some(height) => height.toInt
        case None => "x"
      })
      if (y != (map(0).size - 1)) mapBuilder.append("\r")
    }
    mapBuilder.toString
  }

  // Formula: Math.abs((a mod 360°) - 360°) / 45°
  // Use constants to speed it up!
  def convertDirection(direction: RoomDirection): Int = direction match {
    case RoomDirection.South => 2
    case RoomDirection.SouthWest => 3
    case RoomDirection.SouthEast => 1
    case RoomDirection.West => 4
    case RoomDirection.East => 0
    case RoomDirection.North => 6
    case RoomDirection.NorthWest => 5
    case RoomDirection.NorthEast => 7
  }

  def composeEntityStatuses(stanceProperties: Set[StanceProperty]): String = stanceProperties.map {
    case WalkingTo(Vector3(x, y, z)) => s"mv $x,$y,$z"
  }.mkString("/")
}
