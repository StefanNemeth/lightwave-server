package de.lightwave.shockwave.handler

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.Write
import akka.util.{ByteString, Timeout}
import de.lightwave.rooms.engine.RoomEvent
import de.lightwave.rooms.engine.entity.EntityDirector.SpawnEntity
import de.lightwave.rooms.engine.entity.RoomEntity.{TeleportTo, WalkTo}
import de.lightwave.rooms.engine.entity.{EntityReference, EntityStance, RoomEntity}
import de.lightwave.rooms.engine.mapping.MapCoordinator.GetAbsoluteHeightMap
import de.lightwave.rooms.engine.mapping.RoomMap.StaticMap
import de.lightwave.rooms.engine.mapping.Vector3
import de.lightwave.services.pubsub.Broadcaster.Subscribe
import de.lightwave.shockwave.handler.RoomHandler.SetEntity
import de.lightwave.shockwave.io.protocol.messages._

class RoomHandler(connection: ActorRef, roomEngine: ActorRef) extends Actor with ActorLogging {
  import context.dispatcher
  import akka.pattern._
  import scala.concurrent.duration._

  implicit val timeout = Timeout(5.seconds)

  var entity: Option[ActorRef] = None

  // Make this optional?
  (roomEngine ? SpawnEntity(EntityReference(1, "Steve"))).map {
    case entity: ActorRef => SetEntity(entity)
  } pipeTo self

  // Subscribe to room events
  roomEngine ! Subscribe(self)

  def entityReceive(entity: ActorRef): Receive = {
    case MoveUserMessage(pos) => entity ! WalkTo(pos)
  }

  def messageReceive: Receive = {
    case msg:RoomUserMessage => entity match {
      case Some(e) => entityReceive(e)(msg)
      case None => log.warning("Room user message cannot be handled without entity")
    }
    case GetHeightmapMessage => (roomEngine ? GetAbsoluteHeightMap).map {
      case map:IndexedSeq[_] => Write(HeightmapMessageComposer.compose(map.asInstanceOf[StaticMap[Double]]))
    }.recover {
      case _ => Write(HeightmapMessageComposer.compose(IndexedSeq.empty))
    } pipeTo connection
    case GetUsersMessage =>
      connection ! Write(EntityListMessageComposer.compose(Seq.empty))
      roomEngine ! RoomEntity.GetRenderInformation
    case GetObjectsMessage =>
      connection ! Write(PublicObjectsMessageComposer.compose())
      connection ! Write(FloorItemsMessageComposer.compose())
    case GetItemsMessage =>
      connection ! Write(WallItemsMessageComposer.compose())
  }

  def eventReceive: Receive = {
    case RoomEntity.Spawned(id, reference, _) =>
      connection ! Write(EntityListMessageComposer.compose(Seq((id, reference, Vector3.empty))))
    case RoomEntity.PositionUpdated(id, pos, stance) =>
      connection ! Write(EntityStanceMessageComposer.compose(id, pos, stance))
  }

  override def receive: Receive = messageReceive orElse eventReceive orElse {
    case SetEntity(e) => entity = Some(e)

    // Render entity that was requested by "GetUserStancesMessage"
    // Buffer them?
    case RoomEntity.RenderInformation(id, reference, pos, stance) =>
      connection ! Write(EntityListMessageComposer.compose(Seq((id, reference, pos))) ++ EntityStanceMessageComposer.compose(id, pos, stance))
  }
}

object RoomHandler {
  case class SetEntity(entity: ActorRef)

  def props(connection: ActorRef, roomEngine: ActorRef) = Props(classOf[RoomHandler], connection, roomEngine)
}
