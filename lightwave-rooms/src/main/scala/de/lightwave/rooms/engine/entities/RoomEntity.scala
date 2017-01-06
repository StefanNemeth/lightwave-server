package de.lightwave.rooms.engine.entities

import akka.actor.{Actor, Props}
import akka.actor.Actor.Receive

class RoomEntity(id: Int) extends Actor {
  override def receive: Receive = ???
}

object RoomEntity {
  def props(id: Int) = Props(classOf[RoomEntity], id)
}
