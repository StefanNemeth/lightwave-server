package de.lightwave.rooms.engine.entities

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import de.lightwave.rooms.engine.entities.RoomEntity._
import de.lightwave.rooms.engine.entities.StanceProperty.WalkingTo
import de.lightwave.rooms.engine.mapping.MapCoordinator.GetHeight
import de.lightwave.rooms.engine.mapping.{RoomDirection, Vector2, Vector3}
import de.lightwave.services.pubsub.Broadcaster.Publish
import scala.concurrent.duration._

case class EntityReference(id: Int, name: String)

/**
  * Living object in a room that can be a player, bot or a pet.
  * It interacts using signs, chat messages, dances and moves.
  *
  * @param id Virtual id
  */
class RoomEntity(id: Int, var reference: EntityReference, mapCoordinator: ActorRef, broadcaster: ActorRef) extends Actor {
  import akka.pattern._
  import context.dispatcher

  var position: Vector3 = Vector3.empty
  var stance = RoomEntity.DefaultStance

  var walkDestination: Option[Vector2] = None
  var walking: Boolean = false

  def walkingReceive: Receive = {
    // Walk only if destination is not already reached
    case WalkTo(destination) if destination.x != position.x || destination.y != position.y =>
      walkDestination = Some(destination)

      // Start walking process if not running
      if (!walking) {
        walking = true

        val newPos = Vector2(position.x + 1, position.y)

        // Start walking animation
        self ! SetPosition(position, Some(stance.copy(properties = stance.properties :+ WalkingTo(newPos))))
        self ! WalkOver(newPos)
      }
    case WalkOver(pos) =>
      context.system.scheduler.scheduleOnce(RoomEntity.WalkingSpeed, self, WalkOn(pos))
    case WalkOn(newPos) =>
      position = position.copy(x = newPos.x, y = newPos.y)
      walking = false

      // Continue walking?
      walkDestination match {
        case Some(destination) if destination.x != position.x || destination.y != position.y => self ! WalkTo(destination)
        case _ => self ! FinishWalk
      }
    case FinishWalk =>
      self ! SetPosition(position, Some(stance.copy(properties = stance.properties.filter(!_.isInstanceOf[WalkingTo]))))
      walkDestination = None
      walking = false
  }

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

  val WalkingSpeed: FiniteDuration = 470.milliseconds

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
    * Start walking to specific tile, if already walking
    * change destination
    */
  case class WalkTo(dest: Vector2)

  /**
    * Walk from one tile to another, that should
    * already be blocked
    */
  case class WalkOver(pos: Vector2)

  /**
    * Step onto new tile and finally leave old position.
    * If destination is not reached, continue walking.
    */
  case class WalkOn(newPos: Vector2)

  /**
    * Stop and finish off walk.
    */
  case object FinishWalk

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

  def props(id: Int, reference: EntityReference)(mapCoordinator: ActorRef, broadcaster: ActorRef) = Props(classOf[RoomEntity], id, reference, mapCoordinator, broadcaster)
}