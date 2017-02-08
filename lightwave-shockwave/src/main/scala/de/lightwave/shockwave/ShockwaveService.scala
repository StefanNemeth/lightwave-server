package de.lightwave.shockwave

import java.net.InetSocketAddress

import akka.actor.{ActorLogging, ActorRef, Props}
import de.lightwave.services.Service
import de.lightwave.shockwave.handler.MessageHandler
import de.lightwave.shockwave.io.ConnectionHandler

/**
  * Front-end server which provides game access to Habbo shockwave
  * clients of version v26
  */
class ShockwaveService(endpoint: InetSocketAddress) extends Service with ActorLogging {
  import akka.io._
  import akka.io.Tcp._

  import context.system

  log.info(s"Binding service to $endpoint..")
  IO(Tcp) ! Bind(self, endpoint)

  val messageHandler: ActorRef = context.actorOf(MessageHandler.props(), "MessageHandler")

  override def receive: Receive = {
    case Bound(addr) => log.info(s"Shockwave service bound to $addr and listening")
    case CommandFailed(_: Bind) => context stop self
    case Connected(remote, _) =>
      log.debug("Remote address {} connected", remote)

      val handler = context.actorOf(ConnectionHandler.props(remote, sender(), messageHandler))
      sender() ! Register(handler)
  }
}

object ShockwaveService {
  def props(endpoint: InetSocketAddress): Props = Props(classOf[ShockwaveService], endpoint)
}