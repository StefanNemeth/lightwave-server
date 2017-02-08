package de.lightwave.shockwave.handler

import akka.actor.{Actor, Props}
import de.lightwave.shockwave.io.protocol.message.incoming.miscellaneous.PongMessage

/**
  * Forwards client messages to handlers of a specific role
  */
class MessageHandler extends Actor {
  override def receive: Receive = {
    case _ =>
  }
}

object MessageHandler {
  def props() = Props(classOf[MessageHandler])
}