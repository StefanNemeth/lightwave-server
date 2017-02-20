package de.lightwave.rooms.engine.entities

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.util.Timeout
import de.lightwave.rooms.engine.{EngineComponent, RoomEvent}
import de.lightwave.rooms.engine.EngineComponent.{AlreadyInitialized, Initialize, Initialized}
import de.lightwave.rooms.engine.entities.EntityDirector._
import de.lightwave.rooms.engine.entities.RoomEntity.{GetRenderInformation, TeleportTo}
import de.lightwave.rooms.engine.mapping.MapCoordinator.GetDoorPosition
import de.lightwave.rooms.engine.mapping.Vector2
import de.lightwave.services.pubsub.Broadcaster.Publish

import scala.collection.mutable

trait EntityEvent extends RoomEvent

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
  var spawnPosition: Option[Vector2] = None

  /**
    * Generates a new entity id and spawns an entity assigned to it
    * and teleports it to spawn
    *
    * @return Reference to entity
    */
  def spawnEntity(reference: EntityReference, position: Vector2): ActorRef = {
    val entityId = { idGenerator += 1; idGenerator }
    val entity = context.actorOf(RoomEntity.props(entityId, reference)(mapCoordinator, broadcaster))

    entities += (entityId -> entity)
    broadcaster ! Publish(RoomEntity.Spawned(entityId, reference, entity))

    log.debug(s"Spawning new entity '${reference.name}'")
    entity ! TeleportTo(position)

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
    case SpawnEntityAt(reference, pos) => sender() ! spawnEntity(reference, pos)
    case SpawnEntity(reference) =>
      val replyTo = sender()
      // Get spawn position, if not found fetch it (usually when the room is getting
      // loaded)
      spawnPosition match {
        case None => (mapCoordinator ? GetDoorPosition) (Timeout(3.seconds)).mapTo[Vector2].recover {
          case _ => new Vector2(0, 0)
        }.foreach((position: Vector2) => {
          self ! SetSpawnPosition(position)
          self.tell(SpawnEntityAt(reference, position), replyTo)
        })
        case Some(spawn) => self.tell(SpawnEntityAt(reference, spawn), replyTo)
      }
    case SetSpawnPosition(position) => spawnPosition = Some(position)
    // Get render information of all entities and forward them to the sender
    case cmd @ GetRenderInformation => entities.values.foreach(_ forward cmd)
  }
}

object EntityDirector {
  case class GetEntity(id: Int)
  case class SpawnEntity(reference: EntityReference)
  case class SpawnEntityAt(reference: EntityReference, pos: Vector2)
  case class SetSpawnPosition(position: Vector2)

  def props()(mapCoordinator: ActorRef, broadcaster: ActorRef) = Props(classOf[EntityDirector], mapCoordinator, broadcaster)
}