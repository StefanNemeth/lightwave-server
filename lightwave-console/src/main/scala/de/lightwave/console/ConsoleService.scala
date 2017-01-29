package de.lightwave.console

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.util.Timeout
import de.lightwave.console.rooms.RoomVisualization
import de.lightwave.dedicated.commands.{DedicatedServerCommandContext, DedicatedServerCommandHandler}
import de.lightwave.rooms.RoomService.GetRoom
import de.lightwave.rooms.engine.RoomEngine.{AlreadyInitialized, InitializeRoom, Initialized}
import de.lightwave.rooms.engine.entities.EntityDirector.{EntitySpawned, GetEntity, SpawnEntity}
import de.lightwave.rooms.engine.entities.EntityReference
import de.lightwave.rooms.engine.entities.RoomEntity.{EntityPositionUpdated, TeleportTo}
import de.lightwave.rooms.engine.mapping.RoomMap.StaticMap
import de.lightwave.rooms.engine.mapping.Vector2
import de.lightwave.rooms.model.Room
import de.lightwave.rooms.model.Rooms.RoomId
import de.lightwave.services.Service
import de.lightwave.services.pubsub.Broadcaster.{Subscribe, Unsubscribe}

import scala.concurrent.Future

class ConsoleService(commandHandler: DedicatedServerCommandHandler, roomService: ActorRef, roomRegion: ActorRef) extends Service with ActorLogging {
  import akka.pattern._
  import scala.concurrent.duration._
  import context.dispatcher

  object DefaultContext extends DedicatedServerCommandContext {
    def fetchRoom(id: RoomId): Future[Option[Room]] = (roomService ? GetRoom(id))(Timeout(5.seconds)).mapTo[Option[Room]]

    override def handle(args: Array[String]): PartialFunction[Any, Unit] = {
      case "help" => write("Available commands: fetch-room [id], init-room [id], manage-room [id], watch-room [id]")
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
            context.become(roomManagementReceive)
            roomRegion ! InitializeRoom(room)
          case None => write("Room not found.")
        }
      case "watch-room" =>
        fetchRoom(args(0).toInt).onSuccess {
          case Some(room) =>
            context.become(roomWatchReceive)
            roomRegion ! InitializeRoom(room)
          case None => write("Room not found.")
        }
    }
  }

  class RoomContext(room: ActorRef) extends DedicatedServerCommandContext {
    def getEntity(id: Int): Future[Option[ActorRef]] = (room ? GetEntity(id))(Timeout(5.seconds)).mapTo[Option[ActorRef]]

    override def handle(args: Array[String]): PartialFunction[Any, Unit] = {
      case "help" => write("Available commands: spawn-entity [name], teleport-entity [id] [x;y], exit")
      case "exit" =>
        context.become(receive)
        sender() ! Unsubscribe(self)
        commandHandler.setContext(DefaultContext)
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
    case _ =>
  }

  def roomManagementReceive: Receive = {
    case Initialized | AlreadyInitialized =>
      sender() ! Subscribe(self)
      commandHandler.setContext(new RoomContext(sender()))
    case e => e match {
      case EntityPositionUpdated(_, _) | EntitySpawned(_, _, _) => log.info("Room event: " + e)
      case _ => // Ignore
    }
  }

  var visualization: RoomVisualization = new RoomVisualization(IndexedSeq())

  def roomWatchReceive: Receive = {
    case Initialized | AlreadyInitialized =>
      sender() ! Subscribe(self)
    // Load map
    case e:IndexedSeq[_] =>
      visualization = new RoomVisualization(e.asInstanceOf[StaticMap[Double]])
      visualization.render()
    case EntitySpawned(id, _, _) => visualization.addEntity(id)
    case EntityPositionUpdated(id, pos) => visualization.updateEntityPosition(id, pos)
  }
}

object ConsoleService {
  def props(commandHandler: DedicatedServerCommandHandler, roomService: ActorRef, roomRegion: ActorRef) =
    Props(classOf[ConsoleService], commandHandler: DedicatedServerCommandHandler, roomService: ActorRef, roomRegion: ActorRef)
}
