package de.lightwave.shockwave.connection

import java.net.InetSocketAddress
import java.nio.BufferOverflowException

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.ByteString
import de.lightwave.shockwave.connection.ConnectionHandler.{ParseMessage, ParsedMessage}
import de.lightwave.shockwave.protocol.MessageHeader

/**
  * Handler of clients that are connected to the shockwave server.
  */
class ConnectionHandler(remoteAddress: InetSocketAddress, connection: ActorRef) extends Actor with ActorLogging {
  import akka.io.Tcp._

  context watch connection

  override def preStart(): Unit = {
    log.debug("Starting new handler for {}", remoteAddress)
  }

  override def receive: Receive = receive(None)

  def receive(implicit collected: Option[(MessageHeader, ByteString)]): Receive = {
    case Received(data) => receiveMessage(data, self)
    case ParseMessage(data) => receiveMessage(data, sender())
    case ParsedMessage(header, body) => log.debug(s"Parsed message ${header.operationCode}: ${body.utf8String}")
    case Closed => context stop self
  }

  private def receiveMessage(data: ByteString, recipient: ActorRef)(implicit collected: Option[(MessageHeader, ByteString)]) = collected match {
    case Some((header, collectedBody)) =>
      context.unbecome()
      parseMessage(header, collectedBody ++ data, recipient)
    case None => parseMessage(data, recipient)
  }

  /**
    * Extracts header of the first message, parses all messages and sends them
    * to $recipient
    * @throws BufferOverflowException on data overflow of ConnectionHandler.MAX_PACKET_SIZE
    */
  def parseMessage(data: ByteString, recipient: ActorRef): Unit = if (data.length >= MessageHeader.LENGTH && data.length <= (ConnectionHandler.MAX_PACKET_SIZE - MessageHeader.LENGTH)) {
    parseMessage(MessageHeader.from(data.slice(0, MessageHeader.LENGTH)), data.slice(MessageHeader.LENGTH, data.length), recipient)
  } else {
    throw new BufferOverflowException
  }

  /**
    * Parses data composed of multiple messages without the first header and sends it
    * to $recipient
    * @throws BufferOverflowException on data overflow of ConnectionHandler.MAX_PACKET_SIZE
    */
  def parseMessage(header: MessageHeader, data: ByteString, recipient: ActorRef): Unit = if (data.length <= (ConnectionHandler.MAX_PACKET_SIZE - MessageHeader.LENGTH)) {
    val remainingBytes = data.length

    // If the message is not completely available, wait for the remaining bytes
    if (remainingBytes < header.bodyLength) {
      context.become(receive(Some((header, data.slice(0, remainingBytes)))))

    // Otherwise, handle the packet(s)
    } else if (remainingBytes >= header.bodyLength) {
      // Fire it!
      recipient ! ParsedMessage(header, data.slice(0, header.bodyLength))

      // Handle another packet if there are unread bytes left
      if (remainingBytes > header.bodyLength) {
        parseMessage(data.slice(header.bodyLength, remainingBytes), recipient)
      }
    }
  } else {
    throw new BufferOverflowException
  }
}

object ConnectionHandler {
  val MAX_PACKET_SIZE = 2048

  case class ParseMessage(body: ByteString)

  case class ParsedMessage(header: MessageHeader, body: ByteString)

  def props(remoteAddress: InetSocketAddress, connection: ActorRef) = Props(classOf[ConnectionHandler], remoteAddress, connection)
}