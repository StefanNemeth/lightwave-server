package de.lightwave.rooms.engine

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import de.lightwave.rooms.engine.RoomEngine.{AlreadyInitialized, InitializeRoom, Initialized}
import de.lightwave.rooms.engine.entity.EntityDirector
import de.lightwave.rooms.engine.mapping.MapCoordinator
import de.lightwave.rooms.repository.{RoomModelRepositorySpec, RoomRepositorySpec}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class RoomEngineSpec extends TestKit(ActorSystem("test-system", ConfigFactory.empty))
  with FunSuiteLike
  with DefaultTimeout
  with ImplicitSender
  with BeforeAndAfterAll {

  test("Initialize engine") {
    withActor() { engine =>
      engine ! InitializeRoom(RoomRepositorySpec.expectedRoom)
      expectMsg(Initialized)
    }
  }

  test("Initialize engine only once") {
    withActor() { engine =>
      engine ! InitializeRoom(RoomRepositorySpec.expectedRoom)
      expectMsg(Initialized)

      engine ! InitializeRoom(RoomRepositorySpec.expectedRoom)
      expectMsg(AlreadyInitialized)
    }
  }

  private def withActor()(testCode: ActorRef => Any): Unit = {
    testCode(system.actorOf(RoomEngine.props(
      MapCoordinator.props(RoomModelRepositorySpec.getRepository()), EntityDirector.props()
    )))
  }

  override def afterAll = {
    shutdown()
  }
}
