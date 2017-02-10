package de.lightwave.shockwave.handler

import akka.actor.{Actor, ActorRef, Props}
import de.lightwave.shockwave.io.protocol.messages.{FrontpageMessage, HandshakeMessage}

/**
  * Forwards client messages to handlers of a specific role
  */
class MessageHandler extends Actor {
  val handshakeHandler: ActorRef = context.actorOf(HandshakeHandler.props(), "Handshake")
  val frontpageHandler: ActorRef = context.actorOf(FrontpageHandler.props(), "Frontpage")

  override def receive: Receive = {
    case e:HandshakeMessage => handshakeHandler forward e
    case e:FrontpageMessage => frontpageHandler forward e
  }
}

object MessageHandler {
  def props() = Props(classOf[MessageHandler])
}