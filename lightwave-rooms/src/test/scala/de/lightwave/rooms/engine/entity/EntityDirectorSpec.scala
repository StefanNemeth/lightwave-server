package de.lightwave.rooms.engine.entity

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.lightwave.rooms.engine.EngineComponent.{AlreadyInitialized, Initialize, Initialized}
import de.lightwave.rooms.engine.entity.EntityDirector._
import de.lightwave.rooms.engine.entity.RoomEntity.GetPosition
import de.lightwave.rooms.engine.mapping.MapCoordinator.GetDoorPosition
import de.lightwave.rooms.engine.mapping.{Vector2, Vector3}
import de.lightwave.rooms.repository.RoomRepositorySpec
import de.lightwave.services.pubsub.Broadcaster.Publish
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class EntityDirectorSpec extends TestKit(ActorSystem("test-system", ConfigFactory.empty))
  with FunSuiteLike
  with DefaultTimeout
  with ImplicitSender
  with BeforeAndAfterAll {

  test("Initialize director") {
    withActor() { director =>
      director ! Initialize(RoomRepositorySpec.expectedRoom)
      expectMsg(Initialized)
    }
  }

  test("Initialize director only once") {
    withActor() { director =>
      director ! Initialize(RoomRepositorySpec.expectedRoom)
      expectMsg(Initialized)

      director ! Initialize(RoomRepositorySpec.expectedRoom)
      expectMsg(AlreadyInitialized)
    }
  }

  test("Spawn entity at specific position") {
    withActor() { director =>
      director ! Initialize(RoomRepositorySpec.expectedRoom)
      expectMsg(Initialized)

      director ! SpawnEntityAt(EntityReference(0, ""), new Vector2(1, 0))

      val entity: ActorRef = expectMsgClass(classOf[ActorRef])

      Thread.sleep(3000) // Wait until entity got teleported

      entity ! GetPosition
      expectMsg(new Vector3(1, 0, 0))
    }
  }

  test("Broadcast spawn of entity") {
    val broadcaster = TestProbe()
    val director = TestActorRef[EntityDirector](EntityDirector.props()(TestProbe().ref, broadcaster.ref))

    director ! Initialize(RoomRepositorySpec.expectedRoom)
    expectMsg(Initialized)

    director ! SpawnEntityAt(EntityReference(0, ""), new Vector2(1, 0))
    val entity: ActorRef = expectMsgClass(classOf[ActorRef])

    broadcaster.expectMsg(Publish(RoomEntity.Spawned(1, EntityReference(0, ""), entity)))
  }

  test("Get entity by id") {
    withActor() { director =>
      director ! Initialize(RoomRepositorySpec.expectedRoom)
      expectMsg(Initialized)

      director ! SpawnEntityAt(EntityReference(0, ""), new Vector2(0, 0))
      expectMsgClass(classOf[ActorRef])

      director ! GetEntity(1)
      expectMsgClass(classOf[Some[ActorRef]])
    }
  }

  test("Set spawn position") {
    val director = TestActorRef[EntityDirector](EntityDirector.props()(TestProbe().ref, TestProbe().ref))

    director ! Initialize(RoomRepositorySpec.expectedRoom)
    expectMsg(Initialized)

    director ! SetSpawnPosition(Vector2(1, 1))
    assert(director.underlyingActor.spawnPosition.contains(Vector2(1, 1)))
  }

  test("Set spawn position to door position on initialization") {
    val coordinatorProbe = TestProbe()
    val director = TestActorRef[EntityDirector](EntityDirector.props()(coordinatorProbe.ref, TestProbe().ref))

    director ! Initialize(RoomRepositorySpec.expectedRoom)
    expectMsg(Initialized)

    coordinatorProbe.expectMsg(GetDoorPosition)
    coordinatorProbe.reply(Vector2(1, 1))

    assert(director.underlyingActor.spawnPosition.contains(Vector2(1, 1)))
  }

  test("Spawn entity at default position") {
    val director = TestActorRef[EntityDirector](EntityDirector.props()(TestProbe().ref, TestProbe().ref))

    director ! Initialize(RoomRepositorySpec.expectedRoom)
    expectMsg(Initialized)

    director ! SetSpawnPosition(Vector2(1, 1))
    director ! SpawnEntity(EntityReference(0, ""))

    val entity: ActorRef = expectMsgClass(classOf[ActorRef])

    Thread.sleep(3000) // Wait until entity got teleported

    entity ! GetPosition
    expectMsg(new Vector3(1, 1, 0))
  }

  private def withActor()(testCode: ActorRef => Any): Unit = {
    testCode(system.actorOf(EntityDirector.props()(TestProbe().ref, TestProbe().ref)))
  }

  override def afterAll: Unit = {
    shutdown()
  }
}
