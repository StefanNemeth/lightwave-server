package de.lightwave.shockwave.handler

import akka.actor.{Actor, Props}
import akka.io.Tcp.Write
import de.lightwave.shockwave.io.protocol.messages._

class HandshakeHandler extends Actor {
  override def receive = {
    case InitCryptoMessage => sender() ! Write(CryptoParametersMessageComposer.compose())
    case GenerateKeyMessage =>
      // Don't send key, send session parameters instead
      sender() ! Write(SessionParametersMessageComposer.compose())
  }
}

object HandshakeHandler {
  def props() = Props(classOf[HandshakeHandler])
}
