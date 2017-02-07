package de.lightwave.shockwave.io.protocol.message

import akka.util.ByteString
import de.lightwave.shockwave.io.protocol.message.incoming.miscellaneous.PongMessage
import de.lightwave.shockwave.io.protocol.{MessageReader, MessageWriter}

trait Message

/**
  * Collection of message parsers that are assigned
  * to certain operation codes
  */
object MessageParser {
  private var parsers: Array[MessageParser[_]] = Array(PongMessage)

  private var parsersByOpCode: Map[Short, MessageParser[_]] =
    parsers.flatMap(parser => parser.opCodes.map(_ -> parser)).toMap

  def get(opCode: Short): Option[MessageParser[_]] = parsersByOpCode.get(opCode)
}

trait MessageParser[T] {
  def opCodes: Array[Short]

  def parse(header: MessageHeader, body: ByteString): T = parse(MessageReader.from(header, body))

  def parse(reader: MessageReader): T
}

trait MessageComposer {
  def init(opCode: Short): MessageWriter = new MessageWriter(opCode)
}