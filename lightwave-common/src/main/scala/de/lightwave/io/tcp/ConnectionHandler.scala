package de.lightwave.io.tcp

import java.net.InetSocketAddress
import java.nio.BufferOverflowException

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.ByteString
import de.lightwave.io.tcp.ConnectionHandler.{MessageRead, ReadMessage}
import de.lightwave.io.tcp.protocol.{MessageHeader, MessageHeaderParser, MessageParserLibrary}

/**
  * Connection handler that collects messages based on the size
  * given by the header, parses them and sends them to the handler
  */
class ConnectionHandler(
  remoteAddress: InetSocketAddress,
  connection: ActorRef,
  messageHeaderParser: MessageHeaderParser,
  messageParserLib: MessageParserLibrary,
  messageHandler: ActorRef
) extends Actor with ActorLogging {

  import akka.io.Tcp._

  context watch connection

  override def preStart(): Unit = {
    log.debug("Starting new handler for {}", remoteAddress)
  }

  override def receive: Receive = receive(None)

  def receive(implicit collected: Option[(MessageHeader, ByteString)]): Receive = {
    case Received(data) => receiveMessage(data, self)
    case ReadMessage(data) => receiveMessage(data, sender())
    case MessageRead(header, body) =>
      log.debug(s"Parsing message (${header.operationCode}) ${body.utf8String}")
      messageParserLib.get(header.operationCode) match {
      case Some(parser) => messageHandler ! parser.parse(header, body)
      case None => log.warning(s"Couldn't find parser for message (${header.operationCode}) ${body.utf8String}")
    }
    case cmd @ Write(_, _) => connection forward cmd
    case Closed => context stop self
  }

  private def receiveMessage(data: ByteString, recipient: ActorRef)(implicit collected: Option[(MessageHeader, ByteString)]) = collected match {
    case Some((header, collectedBody)) =>
      context.unbecome()
      readMessage(header, collectedBody ++ data, recipient)
    case None => readMessage(data, recipient)
  }

  /**
    * Extracts header of the first message, reads all messages and sends them
    * to $recipient
    * @throws BufferOverflowException on data overflow of ConnectionHandler.MaxPacketSize
    */
  def readMessage(data: ByteString, recipient: ActorRef): Unit = if (data.length >= messageHeaderParser.headerLength && data.length <= (ConnectionHandler.MaxPacketSize - messageHeaderParser.headerLength)) {
    readMessage(messageHeaderParser.parse(data.slice(0, messageHeaderParser.headerLength)), data.slice(messageHeaderParser.headerLength, data.length), recipient)
  } else {
    throw new BufferOverflowException
  }

  /**
    * Reads data composed of multiple messages without the first header and sends it
    * to $recipient
    * @throws BufferOverflowException on data overflow of ConnectionHandler.MaxPacketSize
    */
  def readMessage(header: MessageHeader, data: ByteString, recipient: ActorRef): Unit = if (data.length <= (ConnectionHandler.MaxPacketSize - messageHeaderParser.headerLength)) {
    val remainingBytes = data.length

    // If the message is not completely available, wait for the remaining bytes
    if (remainingBytes < header.bodyLength) {
      context.become(receive(Some((header, data.slice(0, remainingBytes)))))

      // Otherwise, handle the packet(s)
    } else if (remainingBytes >= header.bodyLength) {
      // Fire it!
      recipient ! MessageRead(header, data.slice(0, header.bodyLength))

      // Handle another packet if there are unread bytes left
      if (remainingBytes > header.bodyLength) {
        readMessage(data.slice(header.bodyLength, remainingBytes), recipient)
      }
    }
  } else {
    throw new BufferOverflowException
  }
}

object ConnectionHandler {
  val MaxPacketSize = 2048

  case class ReadMessage(body: ByteString)

  case class MessageRead(header: MessageHeader, body: ByteString)

  def props(remoteAddress: InetSocketAddress, connection: ActorRef, messageHandler: ActorRef) = Props(classOf[ConnectionHandler], remoteAddress, connection, messageHandler)
}