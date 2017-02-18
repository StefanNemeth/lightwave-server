package de.lightwave.shockwave.handler

import akka.actor.{Actor, ActorRef, Props}
import de.lightwave.shockwave.io.protocol.messages.{FrontpageMessage, HandshakeMessage, NavigatorMessage}

/**
  * Forwards client messages to handlers of a specific role
  */
class MessageHandler(playerService: ActorRef, roomRegion: ActorRef) extends Actor {
  val handshakeHandler: ActorRef = context.actorOf(HandshakeHandler.props(), "Handshake")
  val frontpageHandler: ActorRef = context.actorOf(FrontpageHandler.props(playerService), "Frontpage")
  val navigatorHandler: ActorRef = context.actorOf(NavigatorHandler.props(roomRegion) ,"Navigator")

  override def receive: Receive = {
    case e:HandshakeMessage => handshakeHandler forward e
    case e:FrontpageMessage => frontpageHandler forward e
    case e:NavigatorMessage => navigatorHandler forward e
  }
}

object MessageHandler {
  def props(playerService: ActorRef, roomRegion: ActorRef): Props = Props(classOf[MessageHandler], playerService, roomRegion)
}

