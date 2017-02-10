package de.lightwave.io.tcp

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

class TcpServer(endpoint: InetSocketAddress, connectionHandler: (InetSocketAddress, ActorRef) => Props) extends Actor with ActorLogging {
  import akka.io._
  import akka.io.Tcp._

  import context.system

  log.info(s"Binding service to $endpoint..")
  IO(Tcp) ! Bind(self, endpoint)

  override def receive: Receive = {
    case Bound(addr) => log.info(s"Tcp server bound to $addr and listening")
    case CommandFailed(_: Bind) => context stop self
    case Connected(remote, _) =>
      log.debug("Remote address {} connected", remote)

      val handler = context.actorOf(connectionHandler(remote, sender()))
      sender() ! Register(handler)
  }
}

object TcpServer {
  def props(endpoint: InetSocketAddress, connectionHandler: (InetSocketAddress, ActorRef) => Props) =
    Props(classOf[TcpServer], endpoint, connectionHandler)
}
