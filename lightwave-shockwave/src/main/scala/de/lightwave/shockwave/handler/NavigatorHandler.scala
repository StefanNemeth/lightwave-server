package de.lightwave.shockwave.handler

import akka.actor.{Actor, Props}
import akka.io.Tcp.Write
import de.lightwave.rooms.model.Room
import de.lightwave.shockwave.io.protocol.messages._

class NavigatorHandler extends Actor {
  override def receive = {
    case GetRecommendedRoomsMessage =>
      sender() ! Write(RecommendedRoomListComposer.compose(Seq(NavigatorHandler.TestRoom)))
    case GetFlatInformationMessage(id) =>
      sender() ! Write(FlatInformationMessageComposer.compose(NavigatorHandler.TestRoom))
    case GetLoadingAdvertisementMessage =>
      sender() ! Write(LoadingAdvertisementDataComposer.compose())
  }
}

object NavigatorHandler {
  val TestRoom = Room(Some(1), "Test room", "Test description", Some("model_a"))

  def props() = Props(classOf[NavigatorHandler])
}
