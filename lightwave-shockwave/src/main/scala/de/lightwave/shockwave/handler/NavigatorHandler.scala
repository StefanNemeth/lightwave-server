package de.lightwave.shockwave.handler

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.Write
import akka.util.Timeout
import de.lightwave.rooms.engine.RoomEngine.{AlreadyInitialized, InitializeRoom, Initialized}
import de.lightwave.rooms.model.Room
import de.lightwave.shockwave.io.protocol.messages._

class NavigatorHandler(roomRegion: ActorRef) extends Actor with ActorLogging {
  import context.dispatcher
  import akka.pattern._
  import scala.concurrent.duration._

  implicit val timeout = Timeout(5.seconds)

  override def receive = {
    case GetRecommendedRoomsMessage =>
      sender() ! Write(RecommendedRoomListMessageComposer.compose(Seq(NavigatorHandler.TestRoom)))
    case GetFlatInformationMessage(id) =>
      sender() ! Write(FlatInformationMessageComposer.compose(NavigatorHandler.TestRoom))
    case GetLoadingAdvertisementMessage =>
      sender() ! Write(LoadingAdvertisementDataMessageComposer.compose())
    case RoomDirectoryMessage(roomId) =>
      sender() ! Write(InitiateRoomLoadingMessageComposer.compose)
    case TryFlatMessage(roomId) =>
      sender() ! Write(FlatLetInMessageComposer.compose)
    case GoToFlatMessage(roomId) =>
      val replyTo = sender()
      log.debug(s"Trying to enter room $roomId")

      (roomRegion ? InitializeRoom(NavigatorHandler.TestRoom)).foreach {
        case Initialized | AlreadyInitialized =>
          sender() ! Write(RoomReadyMessageComposer.compose(
            NavigatorHandler.TestRoom.modelId.getOrElse("model_a"), NavigatorHandler.TestRoom.id.getOrElse(1)
          ))
      }
  }
}

object NavigatorHandler {
  val TestRoom = Room(Some(1), "Test room", "Test description", Some("model_a"))

  def props(roomRegion: ActorRef) = Props(classOf[NavigatorHandler], roomRegion)
}