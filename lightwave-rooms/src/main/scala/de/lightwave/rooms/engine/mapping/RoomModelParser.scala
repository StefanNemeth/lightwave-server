package de.lightwave.rooms.engine.mapping

import de.lightwave.rooms.model.RoomModel

/**
  * Parses height maps that are formatted to strings as used in
  * databases
  */
object RoomModelParser {
  def toEmptyMap[T](heightMap: String): RoomMap[T] = {
    val cols = heightMap.split("\\r?\\n")

    if (cols.length < 1) {
      throw new IllegalArgumentException("Invalid height map format: There are no columns.")
    }

    if (cols(0).length() < 1) {
      throw new IllegalArgumentException("Invalid height map format: There are no rows.")
    }

    new RoomMap[T](cols(0).length(), cols.length)
  }

  def toStateMap(model: RoomModel): RoomMap[MapUnit] = {
    val map = toEmptyMap[MapUnit](model.heightMap)
    val doorPosition = Vector2.from(model.doorPosition)
    val cols = model.heightMap.split("\\r?\\n")

    for {
      x <- 0 until map.rows
      y <- 0 until map.cols
    } if (x == doorPosition.x && y == doorPosition.y) {
      map.set(x, y)(Door)
    } else {
      map.set(x, y)(if (cols(y)(x).isDigit) Tile else Void)
    }

    map
  }

  def toHeightMap(model: RoomModel): RoomMap[Double] = {
    val map = toEmptyMap[Double](model.heightMap)
    val cols = model.heightMap.split("\\r?\\n")

    for {
      x <- 0 until map.rows
      y <- 0 until map.cols
    } map.set(x, y)(if (cols(y)(x).isDigit) Integer.parseInt(cols(y)(x).toString) else 0)

    map
  }
}
