package de.lightwave.shockwave.handler

import akka.actor.{Actor, ActorRef, Props}
import de.lightwave.shockwave.io.protocol.messages.{FrontpageMessage, HandshakeMessage}

/**
  * Forwards client messages to handlers of a specific role
  */
class MessageHandler(handshakeProps: Props, frontpageProps: Props) extends Actor {
  val handshakeHandler: ActorRef = context.actorOf(handshakeProps, "Handshake")
  val frontpageHandler: ActorRef = context.actorOf(frontpageProps, "Frontpage")

  override def receive: Receive = {
    case e:HandshakeMessage => handshakeHandler forward e
    case e:FrontpageMessage => frontpageHandler forward e
  }
}

object MessageHandler {
  def props(handshakeProps: Props, frontpageProps: Props) = Props(classOf[MessageHandler], handshakeProps, frontpageProps)

  def props(playerService: ActorRef): Props = props(HandshakeHandler.props(), FrontpageHandler.props(playerService))
}