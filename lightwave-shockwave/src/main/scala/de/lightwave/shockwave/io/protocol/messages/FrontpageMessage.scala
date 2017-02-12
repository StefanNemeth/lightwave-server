package de.lightwave.shockwave.io.protocol.messages

import akka.util.ByteString
import de.lightwave.players.model.Player
import de.lightwave.players.model.Players.PlayerId
import de.lightwave.shockwave.io.protocol._

trait FrontpageMessage extends ShockwaveMessage

/**
  * Log in user on front page
  */
case class LoginMessage(username: String, password: String) extends FrontpageMessage

object LoginMessageParser extends ShockwaveMessageParser[LoginMessage] {
  val opCode = OperationCode.Incoming.Login
  def parse(reader: ShockwaveMessageReader) = LoginMessage(reader.readString, reader.readString)
}

/**
  * Requests player information
  */
case object GetPlayerInformationMessage extends FrontpageMessage

object GetPlayerInfoMessageParser extends ShockwaveMessageParser[GetPlayerInformationMessage.type] {
  val opCode = OperationCode.Incoming.GetPlayerInfo
  def parse(reader: ShockwaveMessageReader) = GetPlayerInformationMessage
}

/**
  * Error message on failed login
  */
object LoginFailedMessageComposer extends ShockwaveMessageComposer {
  def compose(errorMessage: String): ByteString = init(OperationCode.Outgoing.LoginFailed)
    .push(errorMessage)
    .toByteString
}

/**
  * Mark login as successful
  */
object AuthenticatedMessageComposer extends ShockwaveMessageComposer {
  def compose: ByteString = init(OperationCode.Outgoing.Authenticated).toByteString
}

/**
  * Response to player information request
  */
object PlayerInformationMessageComposer extends ShockwaveMessageComposer {
  def compose(player: Player): ByteString = init(OperationCode.Outgoing.PlayerInformation)
    .push(player.id.getOrElse(0).toString)
    .push(player.nickname)
    .push("")
    .push("")
    .push("")
    .push(0)
    .push("ch=s01/53,51,44")
    .push(0)
    .push(0)
    .toByteString
}