package de.lightwave.rooms.engine.entities

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.util.Timeout
import de.lightwave.rooms.engine.EngineComponent
import de.lightwave.rooms.engine.EngineComponent.{AlreadyInitialized, Initialize, Initialized}
import de.lightwave.rooms.engine.entities.EntityDirector._
import de.lightwave.rooms.engine.entities.RoomEntity.TeleportTo
import de.lightwave.rooms.engine.mapping.MapCoordinator.GetDoorPosition
import de.lightwave.rooms.engine.mapping.Vector2
import de.lightwave.services.pubsub.Broadcaster.Publish

import scala.collection.mutable

/**
  * Engine component which is responsible for spawning and removing entities such as
  * bots, players and pets. Complement to the item director.
  */
class EntityDirector(mapCoordinator: ActorRef, broadcaster: ActorRef) extends EngineComponent with ActorLogging {
  import akka.pattern._
  import scala.concurrent.duration._
  import context.dispatcher

  val entities: mutable.HashMap[Int, ActorRef] = mutable.HashMap.empty[Int, ActorRef]

  // TODO: Reimplement
  var idGenerator: Int = 0

  // Spawn new entities here
  var spawnPosition: Vector2 = Vector2(0, 0)

  /**
    * Generates a new entity id and spawns an entity assigned to it
    * and teleports it to spawn
    *
    * @return Reference to entity
    */
  def spawnEntity(reference: EntityReference): ActorRef = {
    val entityId = { idGenerator += 1; idGenerator }
    val entity = context.actorOf(RoomEntity.props(entityId, reference)(mapCoordinator, broadcaster))

    entities += (entityId -> entity)
    broadcaster ! Publish(EntitySpawned(entityId, reference, entity))

    log.debug(s"Spawning new entity '${reference.name}'")

    entity ! TeleportTo(spawnPosition)
    entity
  }

  override def receive: Receive = initialReceive

  def initialReceive: Receive = {
    case Initialize(_) =>
      // Get door position from map coordinator and use it as spawn
      (mapCoordinator ? GetDoorPosition)(Timeout(3.seconds)).mapTo[Vector2].foreach((position: Vector2) => {
        self ! SetSpawnPosition(position)
      })

      context.become(initializedReceive)
      sender() ! Initialized
  }

  def initializedReceive: Receive = {
    case Initialize(_) => sender() ! AlreadyInitialized
    case GetEntity(id) => sender() ! entities.get(id)
    case SpawnEntity(reference) => sender() ! spawnEntity(reference)
    case SetSpawnPosition(position) => spawnPosition = position
  }
}

object EntityDirector {
  case class GetEntity(id: Int)
  case class SpawnEntity(reference: EntityReference)
  case class SetSpawnPosition(position: Vector2)

  case class EntitySpawned(id: Int, reference: EntityReference, entity: ActorRef)

  def props()(mapCoordinator: ActorRef, broadcaster: ActorRef) = Props(classOf[EntityDirector], mapCoordinator, broadcaster)
}