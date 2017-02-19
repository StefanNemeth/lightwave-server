package de.lightwave.rooms.engine

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.sharding.ShardRegion
import de.lightwave.rooms.engine.EngineComponent.Initialize
import de.lightwave.rooms.engine.RoomEngine.{AlreadyInitialized, InitializeRoom, Initialized}
import de.lightwave.rooms.engine.entities.{EntityDirector, RoomEntity}
import de.lightwave.rooms.engine.entities.EntityDirector.{GetEntity, SpawnEntity}
import de.lightwave.rooms.engine.mapping.MapCoordinator
import de.lightwave.rooms.engine.mapping.MapCoordinator.GetAbsoluteHeightMap
import de.lightwave.rooms.model.Room
import de.lightwave.services.pubsub.Broadcaster
import de.lightwave.services.pubsub.Broadcaster.{Subscribe, Unsubscribe}

/**
  * Event concerning the room that can be broadcasted
  */
trait RoomEvent

/**
  * Central part of the room engine managing all objects and live-states
  * through components
  */
class RoomEngine(mapCoordinatorProps: Props, entityDirectorProps: ((ActorRef, ActorRef) => Props)) extends Actor with ActorLogging {
  // Mediator responsible for broadcasting messages (e.g. to front-end servers)
  val broadcaster: ActorRef = context.actorOf(Broadcaster.props("Room-" + self.path.name)(DistributedPubSub(context.system).mediator), "Broadcaster")

  val mapCoordinator: ActorRef = context.actorOf(mapCoordinatorProps, "MapCoordinator")
  val entityDirector: ActorRef = context.actorOf(entityDirectorProps(mapCoordinator, broadcaster), "EntityDirector")

  override def preStart(): Unit = {
    log.debug("Starting new room engine")
  }

  // Initialize all room engine components
  def initialize(room: Room): Unit = {
    log.debug(s"Initializing room '${room.name}'")

    Array(mapCoordinator, entityDirector).foreach(_ ! Initialize(room))
  }

  // Initialize engine before doing stuff
  override def receive: Receive = initialReceive

  def initialReceive: Receive = {
    case InitializeRoom(room) =>
      initialize(room)
      sender() ! Initialized
      context.become(initializedReceive)
  }

  def initializedReceive: Receive = {
    case InitializeRoom(_) => sender() ! AlreadyInitialized

    // Public API
    case msg @ SpawnEntity(_) => entityDirector forward msg
    case msg @ GetEntity(_) => entityDirector forward msg
    case msg @ Subscribe(ref) => broadcaster forward msg
    case msg @ Unsubscribe(ref) => broadcaster forward msg
    case msg @ GetAbsoluteHeightMap => mapCoordinator forward msg
    case msg @ RoomEntity.GetRenderInformation => entityDirector forward msg
  }
}

object RoomEngine {
  case class InitializeRoom(room: Room)

  case object Initialized
  case object AlreadyInitialized

  val shardName = "RoomEngine"

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case s @ InitializeRoom(room) => (getEntityIdFromRoom(room), s)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case InitializeRoom(room) => (math.abs(getEntityIdFromRoom(room).hashCode) % 100).toString
  }

  def props(mapCoordinatorProps: Props, entityDirectorProps: ((ActorRef, ActorRef) => Props)) = Props(classOf[RoomEngine], mapCoordinatorProps, entityDirectorProps)
  def props(): Props = props(MapCoordinator.props(), EntityDirector.props())

  def getEntityIdFromRoom(room: Room): String = room.id match {
    case Some(id) => id.toString
    // Get random id when the room doesn't exist in database
    case None => UUID.randomUUID().toString
  }
}