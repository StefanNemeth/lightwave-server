package de.lightwave.shockwave.connection

import java.net.InetSocketAddress
import java.nio.BufferOverflowException

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.ByteString
import de.lightwave.shockwave.connection.ConnectionHandler.ParsedMessage
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

  def receive(collected: Option[Tuple2[MessageHeader, ByteString]]): Receive = {
    case Received(data) => collected match {
      case Some((header, collectedBody)) =>
        context.unbecome()
        parseMessage(header, collectedBody ++ data)
      case None => parseMessage(data)
    }
    case ParsedMessage(header, body) => log.debug(s"Parsed message ${header.operationCode}: ${body.utf8String}")
    case Closed => context stop self
  }

  /**
    * Extracts header of the first message and parses all messages
    * @throws BufferOverflowException on data overflow of ConnectionHandler.MAX_PACKET_SIZE
    */
  def parseMessage(data: ByteString): Unit = if (data.length >= MessageHeader.LENGTH && data.length <= (ConnectionHandler.MAX_PACKET_SIZE - MessageHeader.LENGTH)) {
    parseMessage(MessageHeader.from(data.slice(0, MessageHeader.LENGTH)), data.slice(MessageHeader.LENGTH, data.length))
  } else {
    throw new BufferOverflowException
  }

  /**
    * Parse data composed of multiple messages without the first header
    * @throws BufferOverflowException on data overflow of ConnectionHandler.MAX_PACKET_SIZE
    */
  def parseMessage(header: MessageHeader, data: ByteString): Unit = if (data.length <= (ConnectionHandler.MAX_PACKET_SIZE - MessageHeader.LENGTH)) {
    val remainingBytes = data.length

    // If the message is not completely available, wait for the remaining bytes
    if (remainingBytes < header.bodyLength) {
      context.become(receive(Some((header, data.slice(0, remainingBytes)))))

    // Otherwise, handle the packet(s)
    } else if (remainingBytes >= header.bodyLength) {
      // Fire it!
      self ! ParsedMessage(header, data.slice(0, header.bodyLength))

      // Handle another packet if there are unread bytes left
      if (remainingBytes > header.bodyLength) {
        parseMessage(data.slice(header.bodyLength, remainingBytes))
      }
    }
  } else {
    throw new BufferOverflowException
  }
}

object ConnectionHandler {
  val MAX_PACKET_SIZE = 2048

  case class ParsedMessage(header: MessageHeader, body: ByteString)

  def props(remoteAddress: InetSocketAddress, connection: ActorRef) = Props(classOf[ConnectionHandler], remoteAddress, connection)
}