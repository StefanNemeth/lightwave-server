package de.lightwave.io.tcp.protocol

import akka.util.ByteString

/**
  * Helper class for reading incoming messages which is
  * mainly used by message parsers
  */
abstract class MessageReader(val header: MessageHeader, val body: ByteString) {
  def bodyLength: Int

  def pointer: Int

  def remainingBytes: Int

  def reset(): Unit

  /**
    * @return next large numbers such as player ids
    */
  def readInt: Int

  /**
    * @return next small number such as operation code
    */
  def readShort: Short

  /**
    * @return next simple string such as nickname
    */
  def readString: String

  /**
    * @return next byte
    */
  def readByte: Byte

  /**
    * @return next boolean value
    */
  def readBoolean: Boolean
}
