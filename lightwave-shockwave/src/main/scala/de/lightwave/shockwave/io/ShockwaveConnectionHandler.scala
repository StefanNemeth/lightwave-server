package de.lightwave.shockwave.io

import java.net.InetSocketAddress

import akka.actor.{ActorRef, Props}
import de.lightwave.io.tcp.ConnectionHandler
import de.lightwave.players.model.Player
import de.lightwave.rooms.model.Rooms.RoomId
import de.lightwave.shockwave.handler.RoomHandler
import de.lightwave.shockwave.io.ShockwaveConnectionHandler.{EnterRoom, GetPlayerInformation, SetPlayerInformation}
import de.lightwave.shockwave.io.protocol.messages.{HelloMessageComposer, RoomMessage}
import de.lightwave.shockwave.io.protocol.{ShockwaveMessageHeader, ShockwaveMessageParser}

/**
  * Handler of clients that are connected to the shockwave server.
  */
class ShockwaveConnectionHandler(remoteAddress: InetSocketAddress, connection: ActorRef, messageHandler: ActorRef)
  extends ConnectionHandler(remoteAddress, connection, ShockwaveMessageHeader, ShockwaveMessageParser, messageHandler) {

  import akka.io.Tcp._

  var playerInformation: Option[Player] = None
  var roomHandler: Option[ActorRef] = None

  override def preStart(): Unit = {
    super.preStart()
    connection ! Write(HelloMessageComposer.compose())
  }

  override def handleMessage(msg: Any): Unit = msg match {
    case e:RoomMessage if roomHandler.isDefined => roomHandler match {
      case Some(handler) => handler forward e
      case None => // Ignore if no room handler set
    }
    case _ => super.handleMessage(msg)
  }

  override def customReceive: Receive = {
    case SetPlayerInformation(player) => playerInformation = Some(player)
    case GetPlayerInformation => sender() ! playerInformation
    case EnterRoom(engine) => roomHandler = Some(
      context.actorOf(RoomHandler.props(engine))
    )
  }
}

object ShockwaveConnectionHandler {
  case class SetPlayerInformation(player: Player)
  case class EnterRoom(engine: ActorRef)
  case object GetPlayerInformation

  def props(messageHandler: ActorRef)(remoteAddress: InetSocketAddress, connection: ActorRef) =
    Props(classOf[ShockwaveConnectionHandler], remoteAddress, connection, messageHandler)
}