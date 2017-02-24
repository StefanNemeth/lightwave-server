package de.lightwave.shockwave.handler

import akka.actor.{Actor, ActorRef, Props}
import de.lightwave.shockwave.handler.MessageHandler.HandleMessage
import de.lightwave.shockwave.io.protocol.messages.{FrontpageMessage, HandshakeMessage, NavigatorMessage}

/**
  * Forwards client messages to handlers of a specific role
  */
class MessageHandler(playerService: ActorRef, roomRegion: ActorRef) extends Actor {
  val handshakeHandler: ActorRef = context.actorOf(HandshakeHandler.props(), "Handshake")
  val frontpageHandler: ActorRef = context.actorOf(FrontpageHandler.props(playerService), "Frontpage")
  val navigatorHandler: ActorRef = context.actorOf(NavigatorHandler.props(roomRegion) ,"Navigator")

  override def receive: Receive = {
    case HandleMessage(msg, authenticated) => handleMessage(authenticated)(msg)
  }

  def handleMessage(authenticated: Boolean): Receive = {
    case msg:HandshakeMessage => handshakeHandler forward msg
    case msg:FrontpageMessage => frontpageHandler forward msg

    case msg if authenticated => msg match {
      case _:NavigatorMessage => navigatorHandler forward msg
    }
  }
}

object MessageHandler {
  case class HandleMessage(msg: Any, authenticated: Boolean = true)

  def props(playerService: ActorRef, roomRegion: ActorRef): Props = Props(classOf[MessageHandler], playerService, roomRegion)
}

