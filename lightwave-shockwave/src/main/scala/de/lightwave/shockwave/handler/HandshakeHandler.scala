package de.lightwave.shockwave.handler

import akka.actor.{Actor, Props}
import akka.io.Tcp.Write
import de.lightwave.shockwave.io.protocol.messages._

class HandshakeHandler extends Actor {
  override def receive = {
    case InitCryptoMessage => sender() ! Write(InitCryptoMessageComposer.compose())
    case GenerateKeyMessage => sender() ! Write(SessionParamsMessageComposer.compose())
  }
}

object HandshakeHandler {
  def props() = Props(classOf[HandshakeHandler])
}
