package de.lightwave.shockwave.io.protocol.messages

import akka.util.ByteString
import de.lightwave.players.model.Players.PlayerId
import de.lightwave.shockwave.io.protocol._

/**
  * All register and login messages that are sent by
  * the user on the frontpage
  */
trait FrontpageMessage extends ShockwaveMessage

case class LoginMessage(username: String, password: String) extends FrontpageMessage
case object GetPlayerInfoMessage extends FrontpageMessage

object LoginMessageParser extends ShockwaveMessageParser[LoginMessage] {
  val opCode = OperationCode.Incoming.Login
  def parse(reader: ShockwaveMessageReader) = LoginMessage(reader.readString, reader.readString)
}

object GetPlayerInfoMessageParser extends ShockwaveMessageParser[GetPlayerInfoMessage.type] {
  val opCode = OperationCode.Incoming.GetPlayerInfo
  def parse(reader: ShockwaveMessageReader) = GetPlayerInfoMessage
}

object LoginFailedMessageComposer extends ShockwaveMessageComposer {
  def compose(errorMessage: String): ByteString = init(OperationCode.Outgoing.LoginFailed)
    .push(errorMessage)
    .toByteString
}

object AuthenticatedMessageComposer extends ShockwaveMessageComposer {
  def compose: ByteString = init(OperationCode.Outgoing.Authenticated).toByteString
}

object PlayerInformationMessageComposer extends ShockwaveMessageComposer {
  def compose(playerId: PlayerId, nickname: String, figure: String, gender: String, mission: String): ByteString = init(OperationCode.Outgoing.PlayerInformation)
    .push(playerId.toString)
    .push(nickname)
    .push(figure)
    .push(gender)
    .push(mission)
    .push(0)
    .push("ch=s01/53,51,44")
    .push(0)
    .push(0)
    .toByteString
}