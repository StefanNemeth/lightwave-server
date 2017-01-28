package de.lightwave.rooms.engine.mapping

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import de.lightwave.rooms.model.Room
import de.lightwave.rooms.engine.EngineComponent.{AlreadyInitialized, Initialize, Initialized}
import de.lightwave.rooms.engine.mapping.MapCoordinator._
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

  test("Use fallback model when initializing coordinator using no model") {
    withActor() { coordinator =>
      coordinator ! Initialize(Room(Some(1), "", "", None))
      expectMsg(InitializedFallback)
    }
  }

  test("Use fallback model when initializing coordinator using non-existent model") {
    withActor() { coordinator =>
      coordinator ! Initialize(Room(Some(1), "", "", Some("non_existent")))
      expectMsg(InitializedFallback)
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

  test("Get state of map") {
    withActor() { coordinator =>
      coordinator ! Initialize(Room(Some(1), "", "", None))
      expectMsg(InitializedFallback)

      coordinator ! GetState(2, 0)
      expectMsg(Some(Tile))
    }
  }

  test("Get height of map") {
    withActor() { coordinator =>
      coordinator ! Initialize(Room(Some(1), "", "", None))
      expectMsg(InitializedFallback)

      coordinator ! GetHeight(2, 0)
      expectMsg(Some(0))
    }
  }

  test("Get door position of map") {
    withActor() { coordinator =>
      coordinator ! Initialize(Room(Some(1), "", "", None))
      expectMsg(InitializedFallback)

      coordinator ! GetDoorPosition
      expectMsg(Vector2(0, 0))
    }
  }

  test("Set state and height of map") {
    withActor() { coordinator =>
      coordinator ! Initialize(Room(Some(1), "", "", None))
      expectMsg(InitializedFallback)

      coordinator ! SetStateAndHeight(2, 0, Door, 2)
      
      coordinator ! GetState(2, 0)
      expectMsg(Some(Door))
      
      coordinator ! GetHeight(2, 0)
      expectMsg(Some(2))
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
