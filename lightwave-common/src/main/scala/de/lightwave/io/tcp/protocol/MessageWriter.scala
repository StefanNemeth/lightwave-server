package de.lightwave.io.tcp.protocol

import akka.util.ByteString

/**
  * Helper class for writing outgoing messages that is mainly
  * used by message composers
  */
abstract class MessageWriter(opCode: Short) {
  def push(s: String): MessageWriter
  def push(bs: ByteString): MessageWriter
  def push(b: Byte): MessageWriter
  def push(i: Int): MessageWriter
  def push(b: Boolean): MessageWriter
  def push(s: Short): MessageWriter

  def toByteString: ByteString
}
