package de.lightwave.shockwave.io.protocol.messages

import akka.util.ByteString
import de.lightwave.shockwave.io.protocol._

trait HandshakeMessage extends ShockwaveMessage

/**
  * Response to ping
  */
case object PongMessage extends HandshakeMessage

object PongMessageParser extends ShockwaveMessageParser[PongMessage.type] {
  val opCode = OperationCode.Incoming.Pong
  def parse(reader: ShockwaveMessageReader) = PongMessage
}

/**
  * Requests crypto parameters
  */
case object InitCryptoMessage extends HandshakeMessage

object InitCryptoMessageParser extends ShockwaveMessageParser[InitCryptoMessage.type] {
  val opCode = OperationCode.Incoming.InitCrypto
  def parse(reader: ShockwaveMessageReader) = InitCryptoMessage
}

/**
  * Requests secret key (which it won't get though)
  */
case object GenerateKeyMessage extends HandshakeMessage

object GenerateKeyMessageParser extends ShockwaveMessageParser[GenerateKeyMessage.type] {
  val opCode = OperationCode.Incoming.GenerateKey
  def parse(reader: ShockwaveMessageReader) = GenerateKeyMessage
}

/**
  * Indicates start of communication
  */
object HelloMessageComposer extends ShockwaveMessageComposer {
  def compose(): ByteString = init(OperationCode.Outgoing.Hello).toByteString
}

/**
  * Requests ping
  */
object PingMessageComposer extends ShockwaveMessageComposer {
  def compose(): ByteString = init(OperationCode.Outgoing.Ping).toByteString
}

/**
  * Something to respond to the client so that it will
  * do.. "something" (perhaps telling it whether to use rc4?)
  */
object CryptoParametersMessageComposer extends ShockwaveMessageComposer {
  def compose(): ByteString = init(OperationCode.Outgoing.CryptoParameters)
    .push(true)
    .push(false)
    .toByteString
}

/**
  * Some session specific settings such as date format
  */
object SessionParametersMessageComposer extends ShockwaveMessageComposer {
  def compose(): ByteString = init(OperationCode.Outgoing.SessionParameters)
    .push("RAHIIIKHJIPAIQAdd-MM-yyyy") // Actually, I don't even want to know
    .toByteString
}

