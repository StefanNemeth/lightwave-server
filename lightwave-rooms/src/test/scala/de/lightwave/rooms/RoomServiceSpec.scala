package de.lightwave.rooms

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import de.lightwave.rooms.RoomService.GetRoom
import de.lightwave.rooms.model.Room
import de.lightwave.rooms.repository.{RoomRepository, RoomRepositorySpec}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike, OneInstancePerTest}

import scala.concurrent.Future

class RoomServiceSpec extends TestKit(ActorSystem("test-system", ConfigFactory.empty))
  with FunSuiteLike
  with DefaultTimeout
  with ImplicitSender
  with BeforeAndAfterAll
  with OneInstancePerTest
  with MockitoSugar {

  import org.mockito.Mockito._

  test("Get room when trying to fetch existent room by id") {
    withActor() { service =>
      service ! GetRoom(RoomRepositorySpec.expectedRoom.id.get)
      expectMsg(Some(RoomRepositorySpec.expectedRoom))
    }
  }

  test("Get nothing when trying to fetch non-existent room by id") {
    withActor() { service =>
      service ! GetRoom(100)
      expectMsg(None)
    }
  }

  test("Cache room on first fetch") {
    val repMock = mock[RoomRepository]
    val service = system.actorOf(RoomService.props(repMock))

    // First fetch
    when(repMock.getById(RoomRepositorySpec.expectedRoom.id.get)).thenReturn(Future.successful(Some(
      RoomRepositorySpec.expectedRoom
    )))

    service ! GetRoom(RoomRepositorySpec.expectedRoom.id.get)
    expectMsg(Some(RoomRepositorySpec.expectedRoom))

    reset(repMock)

    // Second fetch
    service ! GetRoom(RoomRepositorySpec.expectedRoom.id.get)
    verify(repMock, never).getById(RoomRepositorySpec.expectedRoom.id.get)

    system.stop(service)
  }

  test("Cache room on first fetch with pre-cached rooms") {
    val repMock = mock[RoomRepository]
    val service = system.actorOf(RoomService.props(repMock))
    val firstExpectedRoom = Room(Some(2), "Test room", "Test description", Some("model_test"))

    // First fetch of room 2
    when(repMock.getById(firstExpectedRoom.id.get)).thenReturn(Future.successful(Some(
      firstExpectedRoom
    )))

    service ! GetRoom(firstExpectedRoom.id.get)
    expectMsg(Some(firstExpectedRoom))

    reset(repMock)

    // First fetch of room 1
    when(repMock.getById(RoomRepositorySpec.expectedRoom.id.get)).thenReturn(Future.successful(Some(
      RoomRepositorySpec.expectedRoom
    )))

    service ! GetRoom(RoomRepositorySpec.expectedRoom.id.get)
    expectMsg(Some(RoomRepositorySpec.expectedRoom))

    reset(repMock)

    // Second fetch of room 1
    service ! GetRoom(RoomRepositorySpec.expectedRoom.id.get)
    verify(repMock, never).getById(RoomRepositorySpec.expectedRoom.id.get)
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
