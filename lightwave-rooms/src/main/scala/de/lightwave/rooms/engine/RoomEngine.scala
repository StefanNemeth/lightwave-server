package de.lightwave.rooms.engine

import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import de.lightwave.rooms.engine.EngineComponent.Initialize
import de.lightwave.rooms.engine.RoomEngine.{AlreadyInitialized, InitializeRoom, Initialized}
import de.lightwave.rooms.engine.entities.EntityDirector
import de.lightwave.rooms.engine.mapping.MapCoordinator
import de.lightwave.rooms.model.Room

/**
  * Central part of the room engine managing all objects and live-states
  * through components
  */
class RoomEngine(mapCoordinatorProps: Props) extends Actor with ActorLogging {
  val mapCoordinator: ActorRef = context.actorOf(mapCoordinatorProps, "MapCoordinator")
  val entityDirector: ActorRef = context.actorOf(EntityDirector.props(), "EntityDirector")

  override def preStart() = {
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

  def props(mapCoordinatorProps: Props) = Props(classOf[RoomEngine], mapCoordinatorProps)
  def props(): Props = props(MapCoordinator.props())

  def getEntityIdFromRoom(room: Room): String = room.id match {
    case Some(id) => id.toString
    // Get random id when the room doesn't exist in database
    case None => UUID.randomUUID().toString
  }
}