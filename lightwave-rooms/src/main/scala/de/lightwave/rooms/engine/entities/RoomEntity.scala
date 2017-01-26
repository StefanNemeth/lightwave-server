package de.lightwave.rooms.engine.entities

import akka.actor.{Actor, Props}
import akka.actor.Actor.Receive

case class EntityReference(name: String)

/**
  * Living object in a room that can be a player, bot or a pet.
  * It interacts using signs, chat messages, dances and moves.
  *
  * @param id Virtual id
  */
class RoomEntity(id: Int, reference: EntityReference) extends Actor {
  override def receive: Receive = {
    case _ =>
  }
}

object RoomEntity {
  def props(id: Int, reference: EntityReference) = Props(classOf[RoomEntity], id, reference)
}
