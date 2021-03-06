package de.lightwave.shockwave.io.protocol.messages

import akka.util.ByteString
import de.lightwave.io.tcp.protocol.MessageHeader
import de.lightwave.rooms.model.Room
import de.lightwave.rooms.model.Rooms.RoomId
import de.lightwave.shockwave.io.protocol._
import de.lightwave.shockwave.io.protocol.messages.InitiateRoomLoadingMessageComposer.init

trait NavigatorMessage extends ShockwaveMessage

/**
  * Last call for entering a room
  */
case class GoToFlatMessage(roomId: RoomId) extends NavigatorMessage

object GoToFlatMessageParser extends ShockwaveMessageParser[GoToFlatMessage] {
  val opCode = OperationCode.Incoming.GoToFlat
  override def parse(reader: ShockwaveMessageReader) = try {
    GoToFlatMessage(reader.body.utf8String.toInt)
  } catch {
    case _:NumberFormatException => GoToFlatMessage(0)
  }
}
/**
  * Check whether client may enter the room
  */
case class TryFlatMessage(roomId: RoomId) extends NavigatorMessage

object TryFlatMessageParser extends ShockwaveMessageParser[TryFlatMessage] {
  val opCode = OperationCode.Incoming.TryFlat
  override def parse(reader: ShockwaveMessageReader) = try {
    TryFlatMessage(reader.body.utf8String.toInt)
  } catch {
    case _:NumberFormatException => TryFlatMessage(0)
  }
}

/**
  * Sent when client opens room in navigator before
  * starting off room loading (room is full?)
  */
case class RoomDirectoryMessage(roomId: RoomId) extends NavigatorMessage

object RoomDirectoryMessageParser extends ShockwaveMessageParser[RoomDirectoryMessage] {
  val opCode = OperationCode.Incoming.RoomDirectory
  override def parse(reader: ShockwaveMessageReader) = RoomDirectoryMessage(reader.readInt)
}

/**
  * Navigate to specific tab of navigator or reload
  * list
  */
case class NavigateMessage(tabId: Int) extends NavigatorMessage

object NavigateMessageParser extends ShockwaveMessageParser[NavigatorMessage] {
  val opCode = OperationCode.Incoming.Navigate
  override def parse(reader: ShockwaveMessageReader) = {
    reader.readBoolean // Something
    NavigateMessage(reader.readInt)
  }
}

/**
  * Fetch rooms that are recommended to the user (probably most active
  * rooms)
  */
case object GetRecommendedRoomsMessage extends NavigatorMessage

object GetRecommendedRoomsMessageParser extends ShockwaveMessageParser[GetRecommendedRoomsMessage.type] {
  val opCode = OperationCode.Incoming.GetRecommendedRooms
  override def parse(reader: ShockwaveMessageReader) = GetRecommendedRoomsMessage
}

/**
  * Requested before a room is getting loaded. Fetches advertisement
  * that is displayed on the loading screen.
  */
case object GetLoadingAdvertisementMessage extends NavigatorMessage

object GetLoadingAdvertisementMessageParser extends ShockwaveMessageParser[GetLoadingAdvertisementMessage.type] {
  val opCode = OperationCode.Incoming.GetLoadingAdvertisement
  override def parse(reader: ShockwaveMessageReader) = GetLoadingAdvertisementMessage
}

/**
  * Requests private room information
  * @param id Room id
  */
case class GetFlatInformationMessage(id: RoomId) extends NavigatorMessage

object GetFlatInformationMessageParser extends ShockwaveMessageParser[GetFlatInformationMessage] {
  val opCode = OperationCode.Incoming.GetFlatInformation
  override def parse(reader: ShockwaveMessageReader) = try {
      GetFlatInformationMessage(reader.body.utf8String.toInt)
    } catch {
      case _:NumberFormatException => GetFlatInformationMessage(0)
    }
}

/**
  * Makes client join a specific room (e.g. when following a friend)
  */
object RoomForwardMessageComposer extends ShockwaveMessageComposer {
  def compose(id: RoomId): ByteString = init(OperationCode.Outgoing.RoomForward)
    .push(false)
    .push(id)
    .toByteString
}

/**
  * Response to room information request
  */
object FlatInformationMessageComposer extends ShockwaveMessageComposer {
  def compose(room: Room): ByteString = init(OperationCode.Outgoing.FlatInformation)
    .push(1)
    .push(0) // status
    .push(room.id.getOrElse(1))
    .push("Steve") // owner
    .push(room.modelId.getOrElse("model_a")) // room model
    .push(room.name)
    .push(room.description)
    .push(true) // Show owner?
    .push(0)
    .push(0) // category
    .push(20) // habbos inside
    .push(25) // max habbos inside
    .toByteString
}

/**
  * Set image and link that is displayed when loading a room
  * Currently disabled! (STR IMG + TAB + LINK)
  */
object LoadingAdvertisementDataMessageComposer extends ShockwaveMessageComposer {
  def compose(): ByteString = init(OperationCode.Outgoing.LoadingAdvertisementData)
    .push('0'.toByte)
    .toByteString
}

/**
  * Response to GetRecommendedRooms
  * Represents rooms that are recommended to the user (probably most active rooms)
  */
object RecommendedRoomListMessageComposer extends ShockwaveMessageComposer {
  def compose(rooms: Seq[Room]): ByteString = {
    val listMessage = init(OperationCode.Outgoing.RecommendedRoomList).push(rooms.length)

    for (room <- rooms) {
      listMessage.push(room.id.getOrElse(1))
         .push(room.name)
         .push("Steve") // owner
         .push("open") // room status
         .push(0) // current side
         .push(20) // max inside
         .push(room.description)
    }

    listMessage.toByteString
  }
}

/**
  * Marks room requested in RoomDirectoryMessage as enterable
  */
object InitiateRoomLoadingMessageComposer extends ShockwaveMessageComposer {
  val compose: ByteString = init(OperationCode.Outgoing.InitiateRoomLoading).toByteString
}

/**
  * Grands access to a private room
  */
object FlatLetInMessageComposer extends ShockwaveMessageComposer {
  val compose: ByteString = init(OperationCode.Outgoing.FlatLetIn).toByteString
}

/**
  * Tells client that requested room has been started
  */
object RoomReadyMessageComposer extends ShockwaveMessageComposer {
  def compose(model: String, roomId: RoomId): ByteString = init(OperationCode.Outgoing.RoomReady)
    .push(ByteString.fromString(model + " " + roomId))
    .toByteString
}