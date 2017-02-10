package de.lightwave.shockwave.io.protocol.messages

import akka.util.ByteString
import de.lightwave.shockwave.io.protocol._

trait HandshakeMessage extends ShockwaveMessage

/**
  * Message that is sent by the client as a response
  * to a PingMessage
  */
case object PongMessage extends HandshakeMessage

case object InitCryptoMessage extends HandshakeMessage
case object GenerateKeyMessage extends HandshakeMessage

object PongMessageParser extends ShockwaveMessageParser[PongMessage.type] {
  val opCode = OperationCode.Incoming.Pong
  def parse(reader: ShockwaveMessageReader) = PongMessage
}

object InitCryptoMessageParser extends ShockwaveMessageParser[InitCryptoMessage.type] {
  val opCode = OperationCode.Incoming.InitCrypto
  def parse(reader: ShockwaveMessageReader) = InitCryptoMessage
}

object GenerateKeyMessageParser extends ShockwaveMessageParser[GenerateKeyMessage.type] {
  val opCode = OperationCode.Incoming.GenerateKey
  def parse(reader: ShockwaveMessageReader) = GenerateKeyMessage
}

/**
  * Message that is sent by the server to indicate start
  * of communication and to request a pong message
  */
object PingMessageComposer extends ShockwaveMessageComposer {
  def compose(): ByteString = init(OperationCode.Outgoing.Ping).toByteString
}

/**
  * Something to respond to the client so that it will
  * do.. "something" (perhaps telling it whether to use rc4?)
  */
object InitCryptoMessageComposer extends ShockwaveMessageComposer {
  def compose(): ByteString = init(OperationCode.Outgoing.InitCrypto)
    .push(true)
    .push(false)
    .toByteString
}

object SessionParamsMessageComposer extends ShockwaveMessageComposer {
  def compose(): ByteString = init(OperationCode.Outgoing.SessionParams)
    .push("RAHIIIKHJIPAIQAdd-MM-yyyy") // Actually, I don't even want to know
    .toByteString
}

