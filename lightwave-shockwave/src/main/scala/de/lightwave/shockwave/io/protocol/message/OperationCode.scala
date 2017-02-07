package de.lightwave.shockwave.io.protocol.message

object OperationCode {
  object Outgoing {
    val Ping: Short = 0
  }

  object Incoming {
    val Pong: Short = 1
  }
}
