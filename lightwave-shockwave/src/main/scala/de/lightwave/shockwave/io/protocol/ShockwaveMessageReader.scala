package de.lightwave.shockwave.io.protocol

import akka.util.ByteString
import de.lightwave.io.tcp.protocol.{MessageHeader, MessageReader}

/**
  * Helper class for reading incoming Shockwave messages which is
  * mainly used by message parsers
  */
class ShockwaveMessageReader(override val header: MessageHeader, override val body: ByteString) extends MessageReader(header, body) {
  private var _pointer: Int = 0
  val bodyLength: Int = body.length

  def pointer: Int = _pointer

  def remainingBytes: Int = bodyLength - _pointer

  def reset(): Unit = _pointer = 0

  /**
    * @return next large numbers such as player ids
    */
  def readInt: Int = {
    var decodingBytes = 5

    if (remainingBytes <= 0) {
      return 0
    } else if (remainingBytes < 5) {
      decodingBytes = remainingBytes
    }

    try {
      val decoded: (Int, Int) = NumberEncoding.decodeInt(body.slice(_pointer, _pointer + decodingBytes))

      _pointer += decoded._2
      decoded._1
    } catch {
      case _:IllegalArgumentException => 0
    }
  }

  /**
    * @return next small number such as operation code
    */
  def readShort: Short = {
    if (remainingBytes < 2) {
      return 0
    }

    _pointer += 2
    NumberEncoding.decodeShort(body.slice(_pointer - 2, _pointer))
  }

  /**
    * @return next simple string such as nickname
    */
  def readString: String = {
    val length = readShort

    if (remainingBytes < length) {
      return ""
    }

    _pointer += length
    body.slice(_pointer - length, _pointer).utf8String
  }

  /**
    * @return next byte
    */
  def readByte: Byte = {
    if (remainingBytes < 1) {
      return 0.toByte
    }

    _pointer += 1
    body(_pointer - 1)
  }

  /**
    * @return next boolean value
    */
  def readBoolean: Boolean = readByte == 'I'.toByte
}

object ShockwaveMessageReader {
  def from(header: MessageHeader, body: ByteString) = new ShockwaveMessageReader(header, body)
}