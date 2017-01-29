package de.lightwave.console.rooms

import de.lightwave.rooms.engine.mapping.RoomMap.StaticMap
import de.lightwave.rooms.engine.mapping.Vector3

import scala.collection.mutable

class RoomVisualization(map: StaticMap[Double]) {
  private val entities = mutable.HashMap.empty[Int, Vector3]

  def addEntity(id: Int): Unit = {
    entities += (id -> Vector3(0, 0, 0))
    render()
  }

  def updateEntityPosition(id: Int, pos: Vector3): Unit = {
    entities(id) = pos
    render()
  }

  def render(): Unit = {
    // Clear terminal
    print(27.toChar + "[2J")

    val builder = new StringBuilder()
    if (map.nonEmpty && map(0).nonEmpty) {
      for (y <- map(0).indices) {
        for (x <- map.indices) if (entities.count(p => p._2.x == x && p._2.y == y) < 1) map(x)(y) match {
          case Some(height) => builder.append("X")
          case None => builder.append(" ")
        } else builder.append("P")
        builder.append("\n")
      }
    }

    print(builder.toString())
  }
}
