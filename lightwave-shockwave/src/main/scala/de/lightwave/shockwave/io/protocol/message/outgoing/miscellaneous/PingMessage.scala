package de.lightwave.shockwave.io.protocol.message.outgoing.miscellaneous

import akka.util.ByteString
import de.lightwave.shockwave.io.protocol.message.{MessageComposer, OperationCode}

object PingMessage extends MessageComposer {
  def compose(): ByteString = init(OperationCode.Outgoing.Ping).toByteString
}