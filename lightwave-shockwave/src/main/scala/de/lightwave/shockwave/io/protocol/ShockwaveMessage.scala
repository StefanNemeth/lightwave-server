package de.lightwave.shockwave.io.protocol

import akka.util.ByteString
import de.lightwave.io.tcp.protocol._
import de.lightwave.shockwave.io.protocol.messages.{GenerateKeyMessageParser, InitCryptoMessageParser, LoginMessageParser, PongMessageParser}

trait ShockwaveMessage extends Message

/**
  * Collection of message parsers that are assigned
  * to certain operation codes
  */
object ShockwaveMessageParser extends MessageParserLibrary {
  private var parsers: Array[MessageParser[_]] = Array(PongMessageParser, InitCryptoMessageParser, GenerateKeyMessageParser, LoginMessageParser)

  private var parsersByOpCode: Map[Short, MessageParser[_]] =
    parsers.flatMap(parser => parser.opCodes.map(_ -> parser)).toMap

  def get(opCode: Short): Option[MessageParser[_]] = parsersByOpCode.get(opCode)
}

object ShockwaveMessageHeader extends MessageHeaderParser {
  override def headerLength = 5

  override def parse(header: ByteString): MessageHeader = {
    if (header.length < ShockwaveMessageHeader.headerLength) {
      throw new IllegalArgumentException("Invalid header size.")
    }
    val len = NumberEncoding.decodeShort(header.slice(0, 3)) - 2
    MessageHeader(if (len <= 0) 0.toShort else len.toShort, NumberEncoding.decodeShort(header.slice(3, 5)))
  }
}

/**
  * Parsing byte string messages from Shockwave clients to
  * associated message objects
  */
trait ShockwaveMessageParser[T] extends MessageParser[T] {
  def parse(header: MessageHeader, body: ByteString): T = parse(ShockwaveMessageReader.from(header, body))
  def parse(reader: ShockwaveMessageReader): T
}

trait ShockwaveMessageComposer {
  def init(opCode: Short): MessageWriter = new ShockwaveMessageWriter(opCode)
}