package de.lightwave.rooms.engine.mapping

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import de.lightwave.rooms.engine.EngineComponent.{AlreadyInitialized, Initialize, Initialized}
import de.lightwave.rooms.repository.{RoomModelRepositorySpec, RoomRepositorySpec}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class MapCoordinatorSpec extends TestKit(ActorSystem("test-system", ConfigFactory.empty))
  with FunSuiteLike
  with DefaultTimeout
  with ImplicitSender
  with BeforeAndAfterAll {

  test("Initialize coordinator") {
    withActor() { coordinator =>
      coordinator ! Initialize(RoomRepositorySpec.expectedRoom)
      expectMsg(Initialized)
    }
  }

  test("Initialize coordinator only once") {
    withActor() { coordinator =>
      coordinator ! Initialize(RoomRepositorySpec.expectedRoom)
      expectMsg(Initialized)

      coordinator ! Initialize(RoomRepositorySpec.expectedRoom)
      expectMsg(AlreadyInitialized)
    }
  }

  private def withActor()(testCode: ActorRef => Any): Unit = {
    testCode(system.actorOf(MapCoordinator.props(
      RoomModelRepositorySpec.getRepository()
    )))
  }

  override def afterAll = {
    shutdown()
  }
}
