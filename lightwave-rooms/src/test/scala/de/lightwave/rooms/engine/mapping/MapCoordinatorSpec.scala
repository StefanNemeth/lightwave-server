package de.lightwave.rooms.engine.mapping

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit}
import com.typesafe.config.ConfigFactory
import de.lightwave.rooms.model.Room
import de.lightwave.rooms.engine.EngineComponent.{AlreadyInitialized, Initialize, Initialized}
import de.lightwave.rooms.engine.mapping.MapCoordinator._
import de.lightwave.rooms.engine.mapping.pathfinding.TestPathfinder
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
      expectMsg(Some(MapUnit.Tile.Clear))
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

  test("Get absolute heights of map") {
    withActor() { coordinator =>
      coordinator ! Initialize(Room(Some(1), "", "", None))
      expectMsg(InitializedFallback)

      coordinator ! GetAbsoluteHeightMap
      expectMsg(IndexedSeq(
        IndexedSeq(Some(0), Some(0), Some(0), Some(0)),
        IndexedSeq(Some(0), Some(0), Some(0), Some(0)),
        IndexedSeq(Some(0), Some(0), Some(0), Some(0)),
        IndexedSeq(Some(0), Some(0), Some(0), Some(0))
      ))
    }
  }

  test("Set state and height of map") {
    withActor() { coordinator =>
      coordinator ! Initialize(Room(Some(1), "", "", None))
      expectMsg(InitializedFallback)

      coordinator ! SetStateAndHeight(2, 0, MapUnit.Door, 2)
      
      coordinator ! GetState(2, 0)
      expectMsg(Some(MapUnit.Door))
      
      coordinator ! GetHeight(2, 0)
      expectMsg(Some(2))
    }
  }

  test("Block tile") {
    withActor() { coordinator =>
      coordinator ! Initialize(Room(Some(1), "", "", None))
      expectMsg(InitializedFallback)

      coordinator ! BlockTile(1, 0)
      expectNoMsg()

      assert(coordinator.underlyingActor.states.get(1, 0).contains(MapUnit.Tile.Blocked))
    }
  }

  test("Clear tile") {
    withActor() { coordinator =>
      coordinator ! Initialize(Room(Some(1), "", "", None))
      expectMsg(InitializedFallback)

      coordinator.underlyingActor.states.set(1, 0)(MapUnit.Tile.Blocked)

      coordinator ! ClearTile(1, 0)
      expectNoMsg()

      assert(coordinator.underlyingActor.states.get(1, 0).contains(MapUnit.Tile.Clear))
    }
  }

  test("Block tile towards destination") {
    withActor() { coordinator =>
      coordinator ! Initialize(Room(Some(1), "", "", None))
      expectMsg(InitializedFallback)

      coordinator ! BlockTileTowardsDestination(Vector2.empty, Vector2(2))
      expectMsg(Some(Vector3(1)))
    }
  }

  private def withActor()(testCode: TestActorRef[MapCoordinator] => Any): Unit = {
    testCode(TestActorRef[MapCoordinator](MapCoordinator.props(
      RoomModelRepositorySpec.getRepository(), TestPathfinder
    )))
  }

  override def afterAll = {
    shutdown()
  }
}
