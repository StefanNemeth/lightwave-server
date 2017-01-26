package de.lightwave.rooms.repository

import de.lightwave.db.connection.DBComponent
import de.lightwave.db.connection.component.PostgresDBComponent
import de.lightwave.rooms.model.Room
import de.lightwave.rooms.model.Rooms.RoomId

import scala.concurrent.Future

object RoomRepository extends PostgresDBComponent with RoomRepository

private[rooms] trait RoomRepository extends RoomTable { this: DBComponent =>
  import driver.api._

  def getById(id: RoomId): Future[Option[Room]] = db.run { roomTableQuery.filter(_.id === id).result.headOption }
}

private[repository] trait RoomTable { this: DBComponent =>
  import driver.api._

  class RoomTable(tag: Tag) extends Table[Room](tag, "room") {
    val id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    val name = column[String]("name")
    val description = column[String]("description")
    val modelId = column[String]("model_id")
    def * = (id.?, name, description, modelId.?) <> (Room.tupled, Room.unapply)
  }

  protected val roomTableQuery = TableQuery[RoomTable]
}