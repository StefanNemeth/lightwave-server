package de.lightwave.rooms.repository

import de.lightwave.db.connection.component.TestH2DBComponent
import de.lightwave.rooms.model.RoomModel
import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}


class RoomModelRepositorySpec extends FunSuite with ScalaFutures {
  implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  val repository: RoomModelRepository = RoomModelRepositorySpec.getRepository()

  test("Get room model by id") {
    val model = repository.getById(RoomModelRepositorySpec.expectedModel.id.get)

    assert(model === Some(RoomModelRepositorySpec.expectedModel))
  }

  test("Get all room models") {
    val models = repository.getAll()

    assert(models === List(RoomModelRepositorySpec.expectedModel))
  }
}

object RoomModelRepositorySpec {
  def getRepository() = new TestH2DBComponent(Array(
    "lightwave-rooms/src/test/resources/db/schema.sql", "lightwave-rooms/src/test/resources/db/schemadata.sql"
  )) with RoomModelRepository

  val expectedModel = RoomModel(Some("model_test"), "0", "0;0")
}
