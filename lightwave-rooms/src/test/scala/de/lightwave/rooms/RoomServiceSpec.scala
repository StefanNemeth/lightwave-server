package de.lightwave.rooms

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import de.lightwave.rooms.RoomService.GetRoom
import de.lightwave.rooms.repository.{RoomRepository, RoomRepositorySpec}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

import scala.concurrent.Future

class RoomServiceSpec extends TestKit(ActorSystem("test-system", ConfigFactory.empty))
  with FunSuiteLike
  with DefaultTimeout
  with ImplicitSender
  with BeforeAndAfterAll
  with MockitoSugar {

  import org.mockito.Mockito._

  test("Get room by id") {
    withActor() { service =>
      service ! GetRoom(RoomRepositorySpec.expectedRoom.id)
      expectMsg(Some(RoomRepositorySpec.expectedRoom))
    }
  }

  test("Cache room on first fetch") {
    val repMock = mock[RoomRepository]
    val service = system.actorOf(RoomService.props(repMock))

    // First fetch
    when(repMock.getById(RoomRepositorySpec.expectedRoom.id)).thenReturn(Future.successful(Some(
      RoomRepositorySpec.expectedRoom
    )))

    service ! GetRoom(RoomRepositorySpec.expectedRoom.id)
    expectMsg(Some(RoomRepositorySpec.expectedRoom))

    reset(repMock)

    // Second fetch
    service ! GetRoom(RoomRepositorySpec.expectedRoom.id)
    verify(repMock, never).getById(RoomRepositorySpec.expectedRoom.id)
  }

  private def withActor()(testCode: ActorRef => Any): Unit = {
    withActor(RoomRepositorySpec.getRepository())(testCode)
  }

  private def withActor(repository: RoomRepository)(testCode: ActorRef => Any): Unit = {
    testCode(system.actorOf(RoomService.props(repository)))
  }

  override def afterAll = {
    shutdown()
  }
}
