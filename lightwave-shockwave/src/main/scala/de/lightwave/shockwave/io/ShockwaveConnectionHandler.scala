package de.lightwave.shockwave.io

import java.net.InetSocketAddress

import akka.actor.{ActorRef, Props}
import de.lightwave.io.tcp.ConnectionHandler
import de.lightwave.shockwave.io.protocol.messages.PingMessageComposer
import de.lightwave.shockwave.io.protocol.{ShockwaveMessageHeader, ShockwaveMessageParser}

/**
  * Handler of clients that are connected to the shockwave server.
  */
class ShockwaveConnectionHandler(remoteAddress: InetSocketAddress, connection: ActorRef, messageHandler: ActorRef)
  extends ConnectionHandler(remoteAddress, connection, ShockwaveMessageHeader, ShockwaveMessageParser, messageHandler) {

  import akka.io.Tcp._

  override def preStart(): Unit = {
    super.preStart()
    connection ! Write(PingMessageComposer.compose())
  }
}

object ShockwaveConnectionHandler {
  def props(messageHandler: ActorRef)(remoteAddress: InetSocketAddress, connection: ActorRef) =
    Props(classOf[ShockwaveConnectionHandler], remoteAddress, connection, messageHandler)
}