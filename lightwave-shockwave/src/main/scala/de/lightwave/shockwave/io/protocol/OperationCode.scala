package de.lightwave.shockwave.io.protocol

object OperationCode {
  object Outgoing {
    val Ping: Short = 0
    val SessionParams: Short = 257
    val InitCrypto: Short = 277
    val LoginFailed: Short = 33
    val Authenticated: Short = 3
    val PlayerInformation: Short = 5
  }

  object Incoming {
    val Pong: Short = 196
    val InitCrypto: Short = 206
    val GenerateKey: Short = 202
    val Login: Short = 4
    val GetPlayerInfo: Short = 8
  }
}
