package de.lightwave.shockwave.handler

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.io.Tcp.Write
import akka.util.{ByteString, Timeout}
import de.lightwave.players.PlayerService.AuthenticatePlayer
import de.lightwave.players.model.Player
import de.lightwave.shockwave.io.ShockwaveConnectionHandler.{GetPlayerInformation, SetPlayerInformation}
import de.lightwave.shockwave.io.protocol.messages._

class FrontpageHandler(playerService: ActorRef) extends Actor with ActorLogging {
  import context.dispatcher
  import akka.pattern._
  import scala.concurrent.duration._

  implicit val timeout: Timeout = Timeout(5.seconds)

  override def receive = {
    case LoginMessage(username, password) =>
      val replyTo = sender()
      val loginFuture = (playerService ? AuthenticatePlayer(username, password)).mapTo[Option[Player]]

      loginFuture foreach {
        case Some(player) => loginPlayer(replyTo, player)
        case None => replyTo ! Write(LoginFailedMessageComposer.compose("Login Incorrect: Invalid username/password combination."))
      }

    case GetPlayerInformationMessage =>
      val replyTo = sender()

      (replyTo ? GetPlayerInformation).mapTo[Option[Player]].foreach {
        case Some(player) => replyTo ! Write(PlayerInformationMessageComposer.compose(player))
        case None => log.warning("Unauthenticated client trying to fetch player information.")
      }
  }

  def loginPlayer(connection: ActorRef, player: Player): Unit = {
    log.debug(s"Player ${player.nickname} is logging in.")

    connection ! Write(AuthenticatedMessageComposer.compose)
    connection ! SetPlayerInformation(player)
  }
}

object FrontpageHandler {
  def props(playerService: ActorRef) = Props(classOf[FrontpageHandler], playerService)
}
