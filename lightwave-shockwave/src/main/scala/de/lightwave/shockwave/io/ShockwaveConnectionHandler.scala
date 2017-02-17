package de.lightwave.shockwave.io

import java.net.InetSocketAddress

import akka.actor.{ActorRef, Props}
import de.lightwave.io.tcp.ConnectionHandler
import de.lightwave.players.model.Player
import de.lightwave.shockwave.io.ShockwaveConnectionHandler.{GetPlayerInformation, SetPlayerInformation}
import de.lightwave.shockwave.io.protocol.messages.HelloMessageComposer
import de.lightwave.shockwave.io.protocol.{ShockwaveMessageHeader, ShockwaveMessageParser}

/**
  * Handler of clients that are connected to the shockwave server.
  */
class ShockwaveConnectionHandler(remoteAddress: InetSocketAddress, connection: ActorRef, messageHandler: ActorRef)
  extends ConnectionHandler(remoteAddress, connection, ShockwaveMessageHeader, ShockwaveMessageParser, messageHandler) {

  import akka.io.Tcp._

  var playerInformation: Option[Player] = None

  override def preStart(): Unit = {
    super.preStart()
    connection ! Write(HelloMessageComposer.compose())
  }

  override def customReceive: Receive = {
    case SetPlayerInformation(player) => playerInformation = Some(player)
    case GetPlayerInformation => sender() ! playerInformation
  }
}

object ShockwaveConnectionHandler {
  case class SetPlayerInformation(player: Player)
  case object GetPlayerInformation

  def props(messageHandler: ActorRef)(remoteAddress: InetSocketAddress, connection: ActorRef) =
    Props(classOf[ShockwaveConnectionHandler], remoteAddress, connection, messageHandler)
}