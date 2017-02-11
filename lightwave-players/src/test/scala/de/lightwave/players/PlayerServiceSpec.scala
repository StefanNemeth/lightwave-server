package de.lightwave.players

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import de.lightwave.players.PlayerService.{AuthenticatePlayer, GetPlayer, GetPlayerByNickname}
import de.lightwave.players.model.{Player, SecurePlayer}
import de.lightwave.players.repository.{PlayerRepository, PlayerRepositorySpec}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike, OneInstancePerTest}
import org.scalatest.mockito.MockitoSugar

import scala.concurrent.Future

class PlayerServiceSpec extends TestKit(ActorSystem("test-system", ConfigFactory.empty))
  with FunSuiteLike
  with DefaultTimeout
  with ImplicitSender
  with BeforeAndAfterAll
  with OneInstancePerTest
  with MockitoSugar {

  import org.mockito.Mockito._

  test("Get player when trying to fetch existent player by id") {
    withActor() { service =>
      service ! GetPlayer(PlayerRepositorySpec.expectedPlayer.id.get)
      expectMsg(Some(Player.from(PlayerRepositorySpec.expectedPlayer)))
    }
  }

  test("Get nothing when trying to fetch non-existent player by id") {
    withActor() { service =>
      service ! GetPlayer(100)
      expectMsg(None)
    }
  }

  test("Get player when trying to fetch existent player nickname") {
    withActor() { service =>
      service ! GetPlayerByNickname(PlayerRepositorySpec.expectedPlayer.nickname)
      expectMsg(Some(Player.from(PlayerRepositorySpec.expectedPlayer)))
    }
  }

  test("Get nothing when trying to fetch non-existent player by nickname") {
    withActor() { service =>
      service ! GetPlayerByNickname("")
      expectMsg(None)
    }
  }

  test("Cache player on first fetch") {
    val repMock = mock[PlayerRepository]
    val service = system.actorOf(PlayerService.props(repMock))

    // First fetch
    when(repMock.getById(PlayerRepositorySpec.expectedPlayer.id.get)).thenReturn(Future.successful(Some(
      PlayerRepositorySpec.expectedPlayer
    )))

    service ! GetPlayer(PlayerRepositorySpec.expectedPlayer.id.get)
    expectMsg(Some(Player.from(PlayerRepositorySpec.expectedPlayer)))

    reset(repMock)

    // Second fetch
    service ! GetPlayer(PlayerRepositorySpec.expectedPlayer.id.get)
    verify(repMock, never).getById(PlayerRepositorySpec.expectedPlayer.id.get)

    system.stop(service)
  }

  test("Cache player on first fetch with pre-cached players") {
    val repMock = mock[PlayerRepository]
    val service = system.actorOf(PlayerService.props(repMock))
    val firstExpectedPlayer = SecurePlayer(Some(2), "Test", Array.empty, Array.empty)

    // First fetch of player 2
    when(repMock.getById(firstExpectedPlayer.id.get)).thenReturn(Future.successful(Some(
      firstExpectedPlayer
    )))

    service ! GetPlayer(firstExpectedPlayer.id.get)
    expectMsg(Some(Player.from(firstExpectedPlayer)))

    reset(repMock)

    // First fetch of player 1
    when(repMock.getById(PlayerRepositorySpec.expectedPlayer.id.get)).thenReturn(Future.successful(Some(
      PlayerRepositorySpec.expectedPlayer
    )))

    service ! GetPlayer(PlayerRepositorySpec.expectedPlayer.id.get)
    expectMsg(Some(Player.from(PlayerRepositorySpec.expectedPlayer)))

    reset(repMock)

    // Second fetch of player 1
    service ! GetPlayer(PlayerRepositorySpec.expectedPlayer.id.get)
    verify(repMock, never).getById(PlayerRepositorySpec.expectedPlayer.id.get)
  }

  test("Authenticate player with valid credentials") {
    withActor() { service =>
      service ! AuthenticatePlayer("Steve", "test")
      expectMsg(Some(Player.from(PlayerRepositorySpec.expectedPlayer)))
    }
  }

  test("Do not authenticate player with invalid credentials") {
    withActor() { service =>
      service ! AuthenticatePlayer("Steve", "test2")
      expectMsg(None)
    }
  }

  private def withActor()(testCode: ActorRef => Any): Unit = {
    withActor(PlayerRepositorySpec.getRepository)(testCode)
  }

  private def withActor(repository: PlayerRepository)(testCode: ActorRef => Any): Unit = {
    testCode(system.actorOf(PlayerService.props(repository)))
  }

  override def afterAll = {
    shutdown()
  }
}