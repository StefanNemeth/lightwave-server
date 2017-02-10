package de.lightwave.shockwave.handler

import akka.actor.{Actor, ActorLogging, Props}
import de.lightwave.shockwave.io.protocol.messages.LoginMessage

class FrontpageHandler extends Actor with ActorLogging {
  override def receive = {
    case LoginMessage(username, password) => log.debug(s"Client trying to log in as ${username} using password ${password}!")
  }
}

object FrontpageHandler {
  def props() = Props(classOf[FrontpageHandler])
}
