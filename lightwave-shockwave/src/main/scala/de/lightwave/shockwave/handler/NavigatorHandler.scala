package de.lightwave.shockwave.handler

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.Write
import akka.util.Timeout
import de.lightwave.rooms.engine.RoomEngine.{AlreadyInitialized, InitializeRoom, Initialized}
import de.lightwave.rooms.model.Room
import de.lightwave.shockwave.io.ShockwaveConnectionHandler.EnterRoom
import de.lightwave.shockwave.io.protocol.messages._

class NavigatorHandler(roomRegion: ActorRef) extends Actor with ActorLogging {
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
      // Create room engine and pass it to the connection handler
      // Timeout of 5 seconds
      roomRegion.tell(InitializeRoom(NavigatorHandler.TestRoom), context.actorOf(Props(new Actor {
        import scala.concurrent.duration._
        import context.dispatcher

        override def preStart(): Unit = {
          context.system.scheduler.scheduleOnce(5.seconds, self, "")
        }

        def receive: Receive = {
          case Initialized | AlreadyInitialized =>
            replyTo ! EnterRoom(sender())
            replyTo ! Write(RoomReadyMessageComposer.compose(
              NavigatorHandler.TestRoom.modelId.getOrElse("model_a"), NavigatorHandler.TestRoom.id.getOrElse(1)
            ))
            context.stop(self)
          case _ => context.stop(self)
        }
      })))
  }
}

object NavigatorHandler {
  val TestRoom = Room(Some(1), "Test room", "Test room", Some("model_a"))

  def props(roomRegion: ActorRef) = Props(classOf[NavigatorHandler], roomRegion)
}