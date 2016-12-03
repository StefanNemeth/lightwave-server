package de.lightwave.rooms

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata.{DistributedData, LWWMap, LWWMapKey}
import de.lightwave.rooms.RoomService.{GetRoom, PutInCache}
import de.lightwave.rooms.model.Room
import de.lightwave.rooms.model.Rooms.RoomId
import de.lightwave.rooms.repository.RoomRepository

import scala.concurrent.Future

class RoomService(roomRepository: RoomRepository) extends Actor {
  import akka.pattern._
  import context.dispatcher

  implicit val cluster = Cluster(context.system)

  val replicator = DistributedData(context.system).replicator
  val DataKey = LWWMapKey[Room]("room")

  /**
    * Fetches the room from the database and caches it
    */
  def getRoomById(roomId: RoomId): Future[Option[Room]] = {
    val roomFuture = roomRepository.getById(roomId)

    roomFuture foreach {
      case Some(room) => self ! PutInCache(roomId, room)
      case None => // Ignore if not existent
    }

    roomFuture
  }

  override def receive = {
    case GetRoom(id) => replicator ! Get(DataKey, ReadLocal, Some((id, sender())))
    case PutInCache(id, room) => replicator ! Update(DataKey, LWWMap.empty[Room], WriteLocal)(_ + (id.toString -> room))

    case g @ GetSuccess(_, Some((roomId: Int, replyTo: ActorRef))) => g.dataValue match {
      case data: LWWMap[_] => data.get(roomId.toString) match {
        case Some(room) => replyTo ! Some(room)
        case None => getRoomById(roomId) pipeTo replyTo
      }
    }

    case NotFound(_, Some((roomId: Int, replyTo: ActorRef))) => getRoomById(roomId) pipeTo replyTo
  }
}

object RoomService {
  case class GetRoom(id: RoomId)
  case class PutInCache(id: RoomId, room: Room)

  // Use Postgres repository by default
  def props() = Props(classOf[RoomService], RoomRepository)

  def props(roomRepository: RoomRepository) = Props(classOf[RoomService], roomRepository)
}
