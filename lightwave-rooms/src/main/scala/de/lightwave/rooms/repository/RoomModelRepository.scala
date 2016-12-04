package de.lightwave.rooms.repository

import de.lightwave.db.connection.DBComponent
import de.lightwave.db.connection.component.PostgresDBComponent
import de.lightwave.rooms.model.RoomModel
import de.lightwave.rooms.model.RoomModels.RoomModelId

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object RoomModelRepository extends RoomModelRepository with PostgresDBComponent

private[rooms] trait RoomModelRepository extends RoomModelTable { this: DBComponent =>
  import driver.api._

  /**
    * Cache all models immediately (shouldn't be too many)
    */
  val models = Await.result(db.run {
      roomModelQuery.map(model => model.id -> model).result
    }, Duration.Inf).toMap

  def getById(id: RoomModelId): Option[RoomModel] = models.get(id)

  def getAll(): List[RoomModel] = models.values.toList
}

private[repository] trait RoomModelTable { this: DBComponent =>
  import driver.api._

  class RoomModelTable(tag: Tag) extends Table[RoomModel](tag, "room_model") {
    val id = column[String]("id", O.PrimaryKey)
    val heightmap = column[String]("heightmap")
    def * = (id.?, heightmap) <> (RoomModel.tupled, RoomModel.unapply)
  }

  protected val roomModelQuery = TableQuery[RoomModelTable]
}
