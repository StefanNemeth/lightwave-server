package de.lightwave.shockwave.io.protocol.messages

import akka.util.ByteString
import de.lightwave.migration.ShockwaveMigration
import de.lightwave.rooms.engine.entity.{EntityReference, EntityStance}
import de.lightwave.rooms.engine.mapping.RoomMap.StaticMap
import de.lightwave.rooms.engine.mapping.{Vector2, Vector3}
import de.lightwave.shockwave.io.protocol._

/**
  * Messages that concern a room without a room id
  * because it is already known to the room handler
  */
trait RoomMessage extends ShockwaveMessage
trait RoomUserMessage extends RoomMessage

/**
  * Fetches heightmap when loading room
  */
case object GetHeightmapMessage extends RoomMessage

object GetHeightmapMessageParser extends ShockwaveMessageParser[GetHeightmapMessage.type] {
  val opCode = OperationCode.Incoming.GetHeightmap
  override def parse(reader: ShockwaveMessageReader) = GetHeightmapMessage
}

/**
  * Fetches users when loading room
  */
case object GetUsersMessage extends RoomMessage

object GetUsersMessageParser extends ShockwaveMessageParser[GetUsersMessage.type] {
  val opCode = OperationCode.Incoming.GetUsers
  override def parse(reader: ShockwaveMessageReader) = GetUsersMessage
}

/**
  * Fetches floor items and public items
  * when loading room
  */
case object GetObjectsMessage extends RoomMessage

object GetObjectsMessageParser extends ShockwaveMessageParser[GetObjectsMessage.type] {
  val opCode = OperationCode.Incoming.GetObjects
  override def parse(reader: ShockwaveMessageReader) = GetObjectsMessage
}

/**
  * Fetches wall items when loading room
  */
case object GetItemsMessage extends RoomMessage

object GetItemsMessageParser extends ShockwaveMessageParser[GetItemsMessage.type] {
  val opCode = OperationCode.Incoming.GetItems
  override def parse(reader: ShockwaveMessageReader) = GetItemsMessage
}

/**
  * Get user stances: Their direction, whether they dance etc.
  */
case object GetUserStancesMessage extends RoomMessage

object GetUserStancesMessageParser extends ShockwaveMessageParser[GetUserStancesMessage.type] {
  val opCode = OperationCode.Incoming.GetUserStances
  override def parse(reader: ShockwaveMessageReader) = GetUserStancesMessage
}

/**
  * Move user in room to specific tile
  */
case class MoveUserMessage(pos: Vector2) extends RoomUserMessage

object MoveUserMessageParser extends ShockwaveMessageParser[MoveUserMessage] {
  val opCode = OperationCode.Incoming.MoveUser

  override def parse(reader: ShockwaveMessageReader) = MoveUserMessage(
    new Vector2(reader.readShort, reader.readShort)
  )
}

/**
  * Send heightmap of room to client
  */
object HeightmapMessageComposer extends ShockwaveMessageComposer {
  def compose(heightmap: StaticMap[Double]): ByteString = init(OperationCode.Outgoing.Heightmap)
    .push(ByteString.fromString(ShockwaveMigration.composeMap(heightmap)))
    .toByteString
}

/**
  * Display entities that are currently in the room
  * TODO: Add figure, mission, gender
  */
object EntityListMessageComposer extends ShockwaveMessageComposer {
  def compose(entities: Seq[(Int, EntityReference, Vector3)]): ByteString = {
    val msg = init(OperationCode.Outgoing.EntityList)
    entities.foreach {
      case (virtualId, EntityReference(id, name), Vector3(x, y, z)) => // TODO: Filter!
        msg.push(ByteString.fromString(
          s"i:$virtualId\r" +
          s"a:$id\r" +
          s"n:${ShockwaveMessageWriter.filterString(name).replaceAll(" ", "")}\r" +
          s"f:8530319002255042801529510014400\r" +
          s"l:$x $y $z\r" +
          s"c:Test mission\r" +
          s"s:M\r"
        ))
    }
    msg.toByteString
  }
}

/**
  * Update stance of a specific entity
  */
object EntityStanceMessageComposer extends ShockwaveMessageComposer {
  def compose(virtualId: Int, pos: Vector3, stance: EntityStance): ByteString = init(OperationCode.Outgoing.EntityStance)
    .push(ByteString.fromString(
      s"$virtualId ${pos.x},${pos.y},${pos.z}," +
      s"${ShockwaveMigration.convertDirection(stance.headDirection)},${ShockwaveMigration.convertDirection(stance.bodyDirection)}" +
      s"/${ShockwaveMigration.composeEntityStatuses(stance.properties)}/" + "\r"
    )).toByteString
}

/**
  * TODO: Display public room objects
  */
object PublicObjectsMessageComposer extends ShockwaveMessageComposer {
  def compose(): ByteString = init(OperationCode.Outgoing.PublicObjects).toByteString
}

/**
  * TODO: Send floor items to client
  */
object FloorItemsMessageComposer extends ShockwaveMessageComposer {
  def compose(): ByteString = init(OperationCode.Outgoing.FloorItems).push(0).toByteString
}

/**
  * TODO: Send wall items to client
  */
object WallItemsMessageComposer extends ShockwaveMessageComposer {
  def compose(): ByteString = init(OperationCode.Outgoing.WallItems).toByteString
}