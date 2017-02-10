package de.lightwave.io.tcp.protocol

import akka.util.ByteString

trait Message

/**
  * Represents the header of a message
  *
  * @param bodyLength Length of the packet without the header
  * @param operationCode Short number that indicates the
  *                      type of message
  */
case class MessageHeader(bodyLength: Short, operationCode: Short)

trait MessageHeaderParser {
  def headerLength: Int
  def parse(header: ByteString): MessageHeader
}

trait MessageParserLibrary {
  def get(opCode: Short): Option[MessageParser[_]]
}

/**
  * Parsing byte string messages from clients to
  * associated message objects
  */
trait MessageParser[T] {
  def opCode: Short
  def opCodes: Array[Short] = Array(opCode)

  def parse(header: MessageHeader, body: ByteString): T
}