package de.lightwave.shockwave.handler

import akka.actor.{Actor, Props}

class NavigatorHandler extends Actor {
  override def receive = {
    case _ =>
  }
}

object NavigatorHandler {
  def props() = Props(classOf[NavigatorHandler])
}
