package de.lightwave.rooms.engine

import java.util.UUID

import akka.actor.{Actor, Props}
import akka.cluster.sharding.ShardRegion
import de.lightwave.rooms.engine.RoomEngine.{AlreadyInitialized, InitializeRoom, Initialized, SubscribeToRoom}
import de.lightwave.rooms.model.Room

class RoomEngine extends Actor {

  /**
    * Initialize all room engine services
    */
  def initialize(room: Room): Unit = {
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
  def props(): Props = Props[RoomEngine]()

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