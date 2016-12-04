package de.lightwave.rooms.engine

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import de.lightwave.rooms.engine.EngineComponent.Initialize
import de.lightwave.rooms.engine.RoomEngine.{AlreadyInitialized, InitializeRoom, Initialized}
import de.lightwave.rooms.engine.mapping.MapCoordinator
import de.lightwave.rooms.model.Room

class RoomEngine(mapCoordinatorProps: Props) extends Actor {
  val mapCoordinator: ActorRef = context.actorOf(mapCoordinatorProps, "mapCoordinator")

  /**
    * Initialize all room engine services
    */
  def initialize(room: Room): Unit = {
    mapCoordinator ! Initialize(room)
  }

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
  def props(): Props = props(MapCoordinator.props())

  def props(mapCoordinatorProps: Props) = Props(classOf[RoomEngine], mapCoordinatorProps)

  case class InitializeRoom(room: Room)

  case object Initialized
  case object AlreadyInitialized

  def getEntityIdFromRoom(room: Room): String = room.id match {
    case Some(id) => id.toString
    case None => UUID.randomUUID().toString
  }

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case s @ InitializeRoom(room) => (getEntityIdFromRoom(room), s)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case InitializeRoom(room) => (math.abs(getEntityIdFromRoom(room).hashCode) % 100).toString
  }

  val shardName = "RoomEngine"
}