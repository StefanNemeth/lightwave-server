package de.lightwave.rooms.repository

import de.lightwave.db.connection.component.TestH2DBComponent
import de.lightwave.rooms.model.Room
import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

class RoomRepositorySpec extends FunSuite with ScalaFutures {
  implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  val repository: RoomRepository = RoomRepositorySpec.getRepository()

  test("Get room by id") {
    val room = repository.getById(RoomRepositorySpec.expectedRoom.id.get)

    whenReady(room) { room =>
      assert(room === Some(RoomRepositorySpec.expectedRoom))
    }
  }
}

object RoomRepositorySpec {
  def getRepository() = new TestH2DBComponent(Array(
    "lightwave-rooms/src/test/resources/db/schema.sql", "lightwave-rooms/src/test/resources/db/schemadata.sql"
  )) with RoomRepository

  val expectedRoom = Room(Some(1), "Test room", "Test description")
}
