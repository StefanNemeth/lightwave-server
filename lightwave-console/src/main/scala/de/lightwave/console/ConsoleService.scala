package de.lightwave.console

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout
import de.lightwave.dedicated.commands.{DedicatedServerCommandContext, DedicatedServerCommandHandler}
import de.lightwave.rooms.RoomService.GetRoom
import de.lightwave.rooms.engine.RoomEngine.{AlreadyInitialized, InitializeRoom, Initialized}
import de.lightwave.rooms.engine.entities.EntityDirector.{EntitySpawned, GetEntity, SpawnEntity}
import de.lightwave.rooms.engine.entities.EntityReference
import de.lightwave.rooms.engine.entities.RoomEntity.{PositionUpdated, TeleportTo}
import de.lightwave.rooms.engine.mapping.Vector2
import de.lightwave.rooms.model.Room
import de.lightwave.rooms.model.Rooms.RoomId
import de.lightwave.services.Service
import de.lightwave.services.pubsub.Broadcaster.Subscribe

import scala.concurrent.Future

class ConsoleService(commandHandler: DedicatedServerCommandHandler, roomService: ActorRef, roomRegion: ActorRef) extends Service with ActorLogging {
  import akka.pattern._
  import scala.concurrent.duration._
  import context.dispatcher

  object DefaultContext extends DedicatedServerCommandContext {
    def fetchRoom(id: RoomId): Future[Option[Room]] = (roomService ? GetRoom(id))(Timeout(5.seconds)).mapTo[Option[Room]]

    override def handle(args: Array[String]): PartialFunction[Any, Unit] = {
      case "help" => write("Available commands: fetch-room [id], init-room [id], manage-room [id]")
      case "fetch-room" =>
        fetchRoom(args(0).toInt).onSuccess {
          case Some(room) => write("Room details: " + room)
          case None => write("Room not found.")
        }
      case "init-room" =>
        fetchRoom(args(0).toInt).onSuccess {
          case Some(room) => roomRegion.tell(InitializeRoom(room), Actor.noSender)
          case None => write("Room not found.")
        }
      case "manage-room" =>
        fetchRoom(args(0).toInt).onSuccess {
          case Some(room) =>
            write("Entering new context..")
            roomRegion ! InitializeRoom(room)
          case None => write("Room not found.")
        }
    }
  }

  class RoomContext(room: ActorRef) extends DedicatedServerCommandContext {
    def getEntity(id: Int): Future[Option[ActorRef]] = (room ? GetEntity(id))(Timeout(5.seconds)).mapTo[Option[ActorRef]]

    override def handle(args: Array[String]): PartialFunction[Any, Unit] = {
      case "help" => write("Available commands: spawn-entity [name], teleport-entity [id] [x;y], exit")
      case "exit" => commandHandler.setContext(DefaultContext)
      case "spawn-entity" => room ! SpawnEntity(EntityReference(args(0)))
      case "teleport-entity" => getEntity(args(0).toInt).onSuccess( {
        case Some(entity) => entity ! TeleportTo(Vector2.from(args(1)))
        case None => write("Entity not found.")
      })
    }
  }

  override def preStart(): Unit = {
    commandHandler.setContext(DefaultContext)
  }

  override def receive: Receive = {
    case Initialized | AlreadyInitialized =>
      sender() ! Subscribe(self)
      commandHandler.setContext(new RoomContext(sender()))
    case e => e match {
      case PositionUpdated(_, _) | EntitySpawned(_, _) => log.info("Room event: " + e)
      case _ => // Ignore
    }
  }
}

object ConsoleService {
  def props(commandHandler: DedicatedServerCommandHandler, roomService: ActorRef, roomRegion: ActorRef) =
    Props(classOf[ConsoleService], commandHandler: DedicatedServerCommandHandler, roomService: ActorRef, roomRegion: ActorRef)
}
