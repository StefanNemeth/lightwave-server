package de.lightwave.shockwave

import java.net.InetSocketAddress

import akka.actor.{ActorLogging, ActorRef, Props}
import de.lightwave.io.tcp.TcpServer
import de.lightwave.services.Service
import de.lightwave.shockwave.handler.MessageHandler
import de.lightwave.shockwave.io.ShockwaveConnectionHandler

/**
  * Front-end server which provides game access to Habbo shockwave
  * clients of version v?
  */
class ShockwaveService(endpoint: InetSocketAddress, playerService: ActorRef, roomRegion: ActorRef) extends Service with ActorLogging {
  val messageHandler: ActorRef = context.actorOf(MessageHandler.props(playerService, roomRegion), "MessageHandler")
  val tcpServer: ActorRef = context.actorOf(TcpServer.props(endpoint, ShockwaveConnectionHandler.props(messageHandler)))

  override def receive: Receive = {
    case _ =>
  }
}

object ShockwaveService {
  def props(endpoint: InetSocketAddress, playerService: ActorRef, roomRegion: ActorRef): Props =
    Props(classOf[ShockwaveService], endpoint, playerService, roomRegion)
}