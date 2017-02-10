package de.lightwave.shockwave.io.protocol.messages

import akka.util.ByteString
import de.lightwave.shockwave.io.protocol._

trait ConnectionMessage extends ShockwaveMessage

/**
  * Message that is sent by the client as a response
  * to a PingMessage
  */
case object PongMessage extends ConnectionMessage

object PongMessageParser extends ShockwaveMessageParser[PongMessage.type] {
  val opCode = OperationCode.Incoming.Pong

  override def parse(reader: ShockwaveMessageReader): PongMessage.type =
    PongMessage
}

/**
  * Message that is sent by the server to indicate start
  * of communication and to request a pong message
  */
object PingMessageComposer extends ShockwaveMessageComposer {
  def compose(): ByteString = init(OperationCode.Outgoing.Ping).toByteString
}