package de.lightwave.players.model

import de.lightwave.players.model.Players.PlayerId

/**
  * Player representation
  *
  * @param id Id of the player.
  * @param nickname Nickname of the player.
  */
case class Player(id: Option[PlayerId], nickname: String)

object Player {
  def from(secure: SecurePlayer) = Player(secure.id, secure.nickname)
}

/**
  * This class should only be used internally for authentication and never
  * be passed to the view or controller (use Player instead)
  *
  * @param password Password of the player (hashed).
  * @param salt Salt used in password hash.
  *
  */
case class SecurePlayer(
  id: Option[PlayerId],
  nickname: String,
  password: Array[Byte],
  salt: Array[Byte]
)

object Players {
  type PlayerId = Int
}