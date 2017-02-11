package de.lightwave.players.repository

import de.lightwave.db.connection.DBComponent
import de.lightwave.db.connection.component.PostgresDBComponent
import de.lightwave.players.model.SecurePlayer
import de.lightwave.players.model.Players.PlayerId

import scala.concurrent.Future

object PlayerRepository extends PostgresDBComponent with PlayerRepository

private[players] trait PlayerRepository extends PlayerTable { this: DBComponent =>
  import driver.api._

  def getById(id: PlayerId): Future[Option[SecurePlayer]] = db.run { playerTableQuery.filter(_.id === id).result.headOption }

  def getByNickname(nickname: String): Future[Option[SecurePlayer]] = db.run { playerTableQuery.filter(_.nickname === nickname).result.headOption }
}

private[repository] trait PlayerTable { this: DBComponent =>
  import driver.api._

  class RoomTable(tag: Tag) extends Table[SecurePlayer](tag, "player") {
    val id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    val nickname = column[String]("nickname")
    val password = column[Array[Byte]]("password")
    val salt = column[Array[Byte]]("password_salt")
    def * = (id.?, nickname, password, salt) <> (SecurePlayer.tupled, SecurePlayer.unapply)
  }

  protected val playerTableQuery = TableQuery[RoomTable]
}