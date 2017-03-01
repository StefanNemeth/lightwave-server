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

  def convertDirection(direction: RoomDirection): Int = direction match {
    case RoomDirection.South => 2
  }

  def composeEntityStatuses(stanceProperties: Seq[StanceProperty]): String = stanceProperties.map {
    case WalkingTo(Vector3(x, y, z)) => s"mv $x,$y,$z"
  }.mkString("/")
}
