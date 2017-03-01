package de.lightwave.rooms.engine.entities

import akka.actor.Actor.Receive
import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import de.lightwave.rooms.engine.entities.RoomEntity._
import de.lightwave.rooms.engine.mapping.MapCoordinator.GetHeight
import de.lightwave.rooms.engine.mapping.{RoomDirection, Vector2, Vector3}
import de.lightwave.services.pubsub.Broadcaster.Publish

import scala.concurrent.duration._

case class EntityReference(id: Int, name: String)

/**
  * Walking functionality of entities
  */
trait EntityWalking { this: RoomEntity =>
  protected def walkingReceive: Receive = {
    case _ =>
  }
}

/**
  * Living object in a room that can be a player, bot or a pet.
  * It interacts using signs, chat messages, dances and moves.
  *
  * @param id Virtual id
  */
class RoomEntity(id: Int, var reference: EntityReference, mapCoordinator: ActorRef, broadcaster: ActorRef) extends Actor with EntityWalking {
  import akka.pattern._
  import context.dispatcher

  var position: Vector3 = Vector3.empty
  var stance = RoomEntity.DefaultStance

  override def receive: Receive = walkingReceive orElse {
    case TeleportTo(pos) =>
      // Get height of new position and update current entity position
      (mapCoordinator ? GetHeight(pos.x, pos.y))(Timeout(2.seconds)).mapTo[Option[Double]].map {
        case Some(height) => SetPosition(Vector3(pos.x, pos.y, height))
        case None => SetPosition(pos)
      }.recover {
        case _ => SetPosition(pos)
      } pipeTo self
    case SetPosition(pos, newStance) =>
      position = pos
      stance = newStance.getOrElse(stance)
      broadcaster ! Publish(PositionUpdated(id, pos, stance))
    case GetRenderInformation => sender() ! RenderInformation(id, reference, position, stance)
    case GetPosition => sender() ! position
  }
}

object RoomEntity {
  val DefaultStance = EntityStance(properties = Seq.empty,
    headDirection = RoomDirection.South,
    bodyDirection = RoomDirection.South)

  /**
    * Get render information of entity including
    * its id, reference and stance
    */
  case object GetRenderInformation

  /**
    * Get current entity position (Vector3)
    */
  case object GetPosition

  /**
    * Teleport player to specific tile, height
    * is fetched automatically
    */
  case class TeleportTo(pos: Vector2)

  /**
    * Update position of entity (when in doubt, use TeleportTo message)
    */
  case class SetPosition(pos: Vector3, stance: Option[EntityStance] = None)

  /**
    * Response to GetRenderInformation message
    * @param virtualId Unique entity id in room
    * @param reference Some properties of the entity
    * @param position Current entity position
    * @param stance The current entity stance
    */
  case class RenderInformation(virtualId: Int, reference: EntityReference, position: Vector3, stance: EntityStance)

  case class PositionUpdated(id: Int, pos: Vector3, stance: EntityStance) extends EntityEvent
  case class Spawned(id: Int, reference: EntityReference, entity: ActorRef)

  def props(id: Int, reference: EntityReference)(mapCoordinator: ActorRef, broadcaster: ActorRef) =
    Props(classOf[RoomEntity], id, reference, mapCoordinator, broadcaster)
}