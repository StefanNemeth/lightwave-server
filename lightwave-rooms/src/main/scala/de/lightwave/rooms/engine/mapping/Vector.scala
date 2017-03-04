package de.lightwave.rooms.engine.mapping

import scala.math.sqrt

sealed trait RoomDirection

object RoomDirection {
  case object North extends RoomDirection
  case object NorthEast extends RoomDirection
  case object NorthWest extends RoomDirection
  case object South extends RoomDirection
  case object SouthEast extends RoomDirection
  case object SouthWest extends RoomDirection
  case object East extends RoomDirection
  case object West extends RoomDirection

  /**
    * @return direction of a movement or nothing if
    *         there is no movement
    */
  def getMovementDirection(from: Vector2, to: Vector2): Option[RoomDirection] = {
    var dir: Option[RoomDirection] = None

    // Dunno whether it could be done nicer
    if (from.x < to.x) {
      dir = Some(South)
    } else if(from.x > to.x) {
      dir = Some(North)
    } else if (from.y > to.y) {
      return Some(East)
    } else if (from.y < to.y) {
      return Some(West)
    }

    dir.map {
      case South if from.y > to.y => SouthEast
      case South if from.y < to.y => SouthWest
      case North if from.y > to.y => NorthEast
      case North if from.y < to.y => NorthWest
      case ignore => ignore
    }
  }
}

case class Vector2(x: Int = 0, y: Int = 0) {
  def +(that: Vector2) = Vector2(x + that.x, y + that.y)
  def +(that: Vector3) = Vector3(x + that.x, y + that.y)
  def -(that: Vector2) = Vector2(x - that.x, y - that.y)
  def -(that: Vector3) = Vector3(x - that.x, y - that.y)

  def is(v: Vector2): Boolean = x == v.x & y == v.y
  def is(v: Vector3): Boolean = x == v.x && y == v.y

  def +(d: RoomDirection): Vector2 = Vector2.from(this, d)

  def length: Int = sqrt(x.toDouble * x.toDouble + y.toDouble * y.toDouble).toInt
  override def toString: String = s"$x;$y"
}

object Vector2 {
  implicit def toVector3(v: Vector2): Vector3 = Vector3(v.x, v.y)
  implicit def toTuple3(v: Vector2): (Int, Int, Double) = (v.x, v.y, 0)
  implicit def toTuple2(v: Vector2): (Int, Int) = (v.x, v.y)

  val empty = new Vector2(0, 0)

  def from(s: String): Vector2 = {
    val parts = s.split(";").iterator

    try {
      Vector2(parts.next().toInt, parts.next().toInt)
    } catch {
      case _: NumberFormatException => new Vector2(0, 0)
    }
  }

  def from(from: Vector2, to: RoomDirection): Vector2 = to match {
    case RoomDirection.South => from + Vector2(1)
    case RoomDirection.SouthWest => from + Vector2(1, 1)
    case RoomDirection.SouthEast => from + Vector2(1, -1)
    case RoomDirection.East => from + Vector2(0, -1)
    case RoomDirection.West => from + Vector2(0, 1)
    case RoomDirection.North => from + Vector2(-1)
    case RoomDirection.NorthWest => from + Vector2(-1, 1)
    case RoomDirection.NorthEast => from + Vector2(-1, -1)
  }
}

case class Vector3(x: Int = 0, y: Int = 0, z: Double = 0) {
  def +(that: Vector2) = Vector3(x + that.x, y + that.y, z)
  def +(that: Vector3) = Vector3(x + that.x, y + that.y, z + that.z)
  def -(that: Vector2) = Vector3(x - that.x, y - that.y, z)
  def -(that: Vector3) = Vector3(x - that.x, y - that.y, z - that.z)

  def is(v: Vector2): Boolean = x == v.x & y == v.y
  def is(v: Vector3): Boolean = x == v.x && y == v.y && z == v.z

  def +(d: RoomDirection): Vector3 = {
    val tmp = Vector2.from(Vector2(x, y), d)
    Vector3(tmp.x, tmp.y, z)
  }

  def length: Int = sqrt(x.toDouble * x.toDouble + y.toDouble * y.toDouble + z.toDouble * z.toDouble).toInt
  override def toString: String = s"$x;$y;$z"
}

object Vector3 {
  implicit def toVector2(v: Vector3): Vector2 = Vector2(v.x, v.y)
  implicit def toTuple3(v: Vector3): (Int, Int, Double) = (v.x, v.y, v.z)
  implicit def toTuple2(v: Vector3): (Int, Int) = (v.x, v.y)

  val empty = new Vector3(0, 0, 0.0)

  def from(s: String): Vector3 = {
    val parts = s.split(";").iterator

    try {
      Vector3(parts.next().toInt, parts.next().toInt, parts.next().toDouble)
    } catch {
      case _: NumberFormatException => new Vector3(0, 0, 0)
    }
  }
}