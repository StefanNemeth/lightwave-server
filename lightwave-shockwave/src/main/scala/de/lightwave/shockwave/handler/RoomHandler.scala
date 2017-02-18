package de.lightwave.shockwave.handler

import akka.actor.{Actor, ActorRef, Props}

class RoomHandler(roomEngine: ActorRef) extends Actor {
  override def receive = {
    case _ =>
  }
}

object RoomHandler {
  def props(roomEngine: ActorRef) = Props(classOf[RoomHandler], roomEngine)
}
