package de.lightwave.shockwave.handler

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp.Write
import akka.util.Timeout
import de.lightwave.shockwave.io.protocol.messages._

class RoomHandler(roomEngine: ActorRef) extends Actor {
  import context.dispatcher
  import akka.pattern._
  import scala.concurrent.duration._

  implicit val timeout = Timeout(5.seconds)

  override def receive: Receive = {
    case GetHeightmapMessage =>
      val replyTo = sender()
      replyTo ! Write(HeightmapMessageComposer.compose("xxxxxxxxxxxx\rxxxx00000000\rxxxx00000000\rxxxx00000000\rxxxx00000000\rxxxx00000000\rxxxx00000000\rxxxx00000000\rxxxx00000000\rxxxx00000000\rxxxx00000000\\rxxxx00000000\rxxxx00000000\rxxxx00000000\rxxxxxxxxxxxx\rxxxxxxxxxxxx\r"))
      //replyTo ! Write(HeightmapMessageComposer.compose("00000\n00x00"))
    /*
      (roomEngine ? GetAbsoluteHeightMap).foreach {
        case map:StaticMap[Double] =>
          //val convertedMap = new StringBuilder()
          println("Loading map...")
          replyTo ! Write(HeightmapMessageComposer.compose("00000\n00x00"))
      }*/
    case GetUsersMessage =>
      sender() ! Write(EntityListMessageComposer.compose())
    case GetObjectsMessage =>
      sender() ! Write(PublicObjectsMessageComposer.compose())
      sender() ! Write(FloorItemsMessageComposer.compose())
    case GetItemsMessage =>
      sender() ! Write(WallItemsMessageComposer.compose())
  }
}

object RoomHandler {
  def props(roomEngine: ActorRef) = Props(classOf[RoomHandler], roomEngine)
}
