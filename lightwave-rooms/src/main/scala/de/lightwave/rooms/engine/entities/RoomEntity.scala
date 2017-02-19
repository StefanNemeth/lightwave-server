package de.lightwave.rooms.engine.entities

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import de.lightwave.rooms.engine.entities.RoomEntity.{GetRenderInformation, PositionUpdated, SetPosition, TeleportTo}
import de.lightwave.rooms.engine.mapping.MapCoordinator.GetHeight
import de.lightwave.rooms.engine.mapping.{Vector2, Vector3}
import de.lightwave.rooms.model.Rooms.RoomId
import de.lightwave.services.pubsub.Broadcaster.Publish

case class EntityReference(id: Int, name: String)
case class EntityStance(pos: Vector3, headDirection: Int, bodyDirection: Int) // todo use enums

/**
  * Living object in a room that can be a player, bot or a pet.
  * It interacts using signs, chat messages, dances and moves.
  *
  * @param id Virtual id
  */
class RoomEntity(id: Int, var reference: EntityReference, mapCoordinator: ActorRef, broadcaster: ActorRef) extends Actor {
  import akka.pattern._
  import scala.concurrent.duration._
  import context.dispatcher

  var position = new Vector3(0, 0, 0.0)

  override def receive: Receive = {
    case TeleportTo(pos) =>
      // Get height of new position and update current entity position
      (mapCoordinator ? GetHeight(pos.x, pos.y))(Timeout(5.seconds)).mapTo[Option[Double]].map {
        case Some(height) => SetPosition(Vector3(pos.x, pos.y, height))
        case None => SetPosition(pos)
      } pipeTo self

    case SetPosition(pos) =>
      position = pos
      broadcaster ! Publish(PositionUpdated(id, pos))

    case GetRenderInformation => sender() ! (id, reference, EntityStance(position, 2, 2))
  }
}

object RoomEntity {
  /**
    * Get render information of entity including
    * its id, reference and stance
    */
  case object GetRenderInformation

  case class TeleportTo(pos: Vector2)
  case class SetPosition(pos: Vector3)

  case class PositionUpdated(id: Int, pos: Vector3) extends EntityEvent

  def props(id: Int, reference: EntityReference)(mapCoordinator: ActorRef, broadcaster: ActorRef) = Props(classOf[RoomEntity], id, reference, mapCoordinator, broadcaster)
}