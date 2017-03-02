package de.lightwave.rooms.engine.entity

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import de.lightwave.rooms.engine.entity.RoomEntity._
import de.lightwave.rooms.engine.entity.StanceProperty.WalkingTo
import de.lightwave.rooms.engine.mapping.MapCoordinator.{BlockTileTowardsDestination, GetHeight}
import de.lightwave.rooms.engine.mapping.{RoomDirection, Vector2, Vector3}
import de.lightwave.services.pubsub.Broadcaster.Publish

import scala.concurrent.duration._

case class EntityReference(id: Int, name: String)

trait EntityWalking { this: RoomEntity =>
  import context.dispatcher
  import akka.pattern._

  private var walkDestination: Option[Vector2] = None
  private var walking: Boolean = false

  protected def walkingReceive: Receive = {
    case WalkTo(destination) if !destination.is(position) =>
      walkDestination = Some(destination)
      if (!walking) {
        walking = true
        (mapCoordinator ? BlockTileTowardsDestination(destination.x, destination.y))(Timeout(2.seconds)).mapTo[Option[Vector3]].map {
          case Some(pos) => WalkOver(pos)
          case None => FinishWalk
        }.recover {
          case _ => FinishWalk
        } pipeTo self
      }
    case WalkOver(pos) =>
      stance = stance.copy(properties = stance.properties :+ WalkingTo(pos))
      broadcastPosition()
      context.system.scheduler.scheduleOnce(RoomEntity.WalkingSpeed, self, WalkOn(pos))
    case WalkOn(newPos) =>
      position = newPos
      walking = false
      self ! (walkDestination match {
        case Some(destination) if !destination.is(position) => WalkTo(destination)
        case _ => FinishWalk
      })
    case FinishWalk =>
      stance = stance.copy(properties = stance.properties.filter(!_.isInstanceOf[WalkingTo]))
      broadcastPosition()
      walkDestination = None
      walking = false
  }
}

/**
  * Living object in a room that can be a player, bot or a pet.
  * It interacts using signs, chat messages, dances and moves.
  *
  * @param id Virtual id
  */
class RoomEntity(id: Int, var reference: EntityReference, val mapCoordinator: ActorRef, broadcaster: ActorRef) extends Actor with EntityWalking {
  import akka.pattern._
  import context.dispatcher

  var position: Vector3 = Vector3.empty
  var stance = RoomEntity.DefaultStance

  def broadcastPosition(): Unit = {
    broadcaster ! Publish(PositionUpdated(id, position, stance))
  }

  override def receive: Receive = walkingReceive orElse {
    case TeleportTo(pos) =>
      (mapCoordinator ? BlockTileTowardsDestination(pos.x, pos.y))(Timeout(2.seconds)).mapTo[Option[Vector3]].map {
        case Some(newPos) => SetPosition(newPos)
        case None => SetPosition(pos)
      }.recover {
        case _ => SetPosition(pos)
      } pipeTo self
    case SetPosition(pos) =>
      position = pos
      broadcastPosition()
    case GetRenderInformation => sender() ! RenderInformation(id, reference, position, stance)
    case GetPosition => sender() ! position
  }
}

object RoomEntity {
  val DefaultStance = EntityStance(properties = Seq.empty,
    headDirection = RoomDirection.South,
    bodyDirection = RoomDirection.South)

  val WalkingSpeed: FiniteDuration = 500.milliseconds

  case object GetRenderInformation
  case object GetPosition

  case class TeleportTo(pos: Vector2)
  case class SetPosition(pos: Vector3)

  /**
    * Start walking to specific tile, if already walking
    * change destination
    */
  case class WalkTo(dest: Vector2)

  /**
    * Walk from one tile to another, that should
    * already be blocked
    */
  case class WalkOver(pos: Vector3)

  /**
    * Step onto new tile and finally leave old position.
    * If destination is not reached, continue walking.
    */
  case class WalkOn(newPos: Vector3)

  /**
    * Stop and finish off walk.
    */
  case object FinishWalk

  case class RenderInformation(virtualId: Int, reference: EntityReference, position: Vector3, stance: EntityStance)

  case class PositionUpdated(id: Int, pos: Vector3, stance: EntityStance) extends EntityEvent
  case class Spawned(id: Int, reference: EntityReference, entity: ActorRef)

  def props(id: Int, reference: EntityReference)(mapCoordinator: ActorRef, broadcaster: ActorRef) =
    Props(classOf[RoomEntity], id, reference, mapCoordinator, broadcaster)
}