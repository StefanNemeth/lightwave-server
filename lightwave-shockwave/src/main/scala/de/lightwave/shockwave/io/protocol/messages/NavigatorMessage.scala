package de.lightwave.shockwave.io.protocol.messages

import akka.util.ByteString
import de.lightwave.rooms.model.Room
import de.lightwave.rooms.model.Rooms.RoomId
import de.lightwave.shockwave.io.protocol._

trait NavigatorMessage extends ShockwaveMessage

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