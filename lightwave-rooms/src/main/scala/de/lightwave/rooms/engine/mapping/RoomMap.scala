package de.lightwave.rooms.engine.mapping

import de.lightwave.rooms.engine.mapping.RoomMap.StaticMap

/**
  * Represents a mutable room map and specific information regarding its units.
  * E.g. their heights or whether they are walkable.
  */
class RoomMap[T](val rows: Int, val cols: Int) {
  if (rows < 0 || cols < 0) {
    throw new IllegalArgumentException("Rows and columns size may not be less than zero!")
  }

  private val underlyingMap = Array.ofDim[Option[T]](rows, cols)

  // Initialize default values
  for {
    x <- 0 until rows
    y <- 0 until cols
  } underlyingMap(x)(y) = None

  /**
    * Check whether tile exists (doesn't check its value!)
    *
    * @param x Row
    * @param y Column
    */
  def exists(x: Int, y: Int): Boolean = x >= 0 && y >= 0 && x <= (rows - 1) && y <= (cols - 1)

  /**
    * @param x Row
    * @param y Column
    * @return Some(state) if tile is set and None if it holds the default value
    *         or doesn't exist
    */
  def get(x: Int, y: Int): Option[T] = if (exists(x, y)) underlyingMap(x)(y) else None

  /**
    * Set state of a tile if it exists.
    * @param x Row
    * @param y Column
    * @param value New state of the tile
    */
  def set(x: Int, y: Int)(value: T): Unit = if (exists(x, y)) {
    underlyingMap(x)(y) = Some(value)
  }

  def toStatic: StaticMap[T] = underlyingMap.map(x => x.toIndexedSeq).toIndexedSeq

  override def toString: String = s"${rows}x${cols} Room map"
}

object RoomMap {
  type StaticMap[T] = IndexedSeq[IndexedSeq[Option[T]]]
}

sealed trait MapUnit

case object Void extends MapUnit
case object Tile extends MapUnit
case object Door extends MapUnit