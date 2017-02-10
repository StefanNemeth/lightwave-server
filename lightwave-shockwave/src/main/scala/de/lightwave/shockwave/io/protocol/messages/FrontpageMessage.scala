package de.lightwave.shockwave.io.protocol.messages

import de.lightwave.shockwave.io.protocol.{OperationCode, ShockwaveMessage, ShockwaveMessageParser, ShockwaveMessageReader}

/**
  * All messages that are sent by user that are not
  * logged in.
  */
trait FrontpageMessage extends ShockwaveMessage

case class LoginMessage(username: String, password: String) extends FrontpageMessage

object LoginMessageParser extends ShockwaveMessageParser[LoginMessage] {
  val opCode = OperationCode.Incoming.Login
  def parse(reader: ShockwaveMessageReader) = LoginMessage(reader.readString, reader.readString)
}