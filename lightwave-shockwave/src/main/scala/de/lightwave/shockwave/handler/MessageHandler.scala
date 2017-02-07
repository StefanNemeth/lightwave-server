package de.lightwave.shockwave.handler

import akka.actor.Actor

/**
  * Forwards client messages to handlers of a specific role
  */
class MessageHandler extends Actor {
  override def receive: Receive = {
    case _ =>
  }
}
