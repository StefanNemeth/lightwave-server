package de.lightwave.shockwave.io.protocol.messages

import akka.util.ByteString
import de.lightwave.shockwave.io.protocol._

/**
  * Messages that concern a room without a room id
  * because it is already known to the room handler
  */
trait RoomMessage extends ShockwaveMessage

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
  * Send heightmap of room to client
  */
object HeightmapMessageComposer extends ShockwaveMessageComposer {
  def compose(heightmap: String): ByteString = init(OperationCode.Outgoing.Heightmap)
    .push(heightmap)
    .toByteString
}

/**
  * TODO: Send entities that are currently in the room
  * to the client
  */
object EntityListMessageComposer extends ShockwaveMessageComposer {
  def compose(): ByteString = init(OperationCode.Outgoing.EntityList)
    //.push("i:1\na:1\nn:Steve\nf:\nl:3 5 0.0\nc:Hey\n")
    .toByteString
}

/**
  * TODO: Let the client display public room objects
  */
object PublicObjectsMessageComposer extends ShockwaveMessageComposer {
  def compose(): ByteString = init(OperationCode.Outgoing.PublicObjects).push(0).toByteString
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