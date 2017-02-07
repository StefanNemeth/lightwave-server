package de.lightwave.shockwave.io.protocol.message.incoming.miscellaneous

import de.lightwave.shockwave.io.protocol.MessageReader
import de.lightwave.shockwave.io.protocol.message.{Message, MessageParser, OperationCode}

case class PongMessage() extends Message

object PongMessage extends MessageParser[PongMessage] {
  val opCodes = Array(OperationCode.Incoming.Pong)

  override def parse(reader: MessageReader): PongMessage = PongMessage()
}
