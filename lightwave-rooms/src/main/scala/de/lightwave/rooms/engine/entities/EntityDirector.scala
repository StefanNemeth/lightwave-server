package de.lightwave.rooms.engine.entities

import akka.actor.Actor.Receive
import akka.actor.{ActorRef, Props}
import de.lightwave.rooms.engine.EngineComponent
import de.lightwave.rooms.engine.EngineComponent.{AlreadyInitialized, Initialize, Initialized}

import scala.collection.mutable

/**
  * Engine component which is responsible for spawning and removing entities such as
  * bots, players and pets. Complement to the item director.
  */
class EntityDirector extends EngineComponent {
  private val entities = mutable.HashMap.empty[Int, ActorRef]

  // TODO: Reimplement
  private var idGenerator: Int = 0

  /**
    * Generates a new entity id and spawns an entity assigned to it
    * @return Reference to entity
    */
  def spawnEntity(): ActorRef = {
    val entityId = { idGenerator += 1; idGenerator }
    val entity = context.actorOf(RoomEntity.props(entityId))

    entities += (entityId -> entity)
    entity
  }

  override def receive: Receive = initialReceive

  def initialReceive: Receive = {
    case Initialize(_) =>
      context.become(initializedReceive)
      sender() ! Initialized
  }

  def initializedReceive: Receive = {
    case Initialize(_) => sender() ! AlreadyInitialized
  }
}

object EntityDirector {
  def props() = Props(classOf[EntityDirector])
}