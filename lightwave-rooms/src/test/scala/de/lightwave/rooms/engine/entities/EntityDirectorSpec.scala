package de.lightwave.rooms.engine.entities

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.lightwave.rooms.engine.EngineComponent.{AlreadyInitialized, Initialize, Initialized}
import de.lightwave.rooms.engine.entities.EntityDirector.{GetEntity, SetSpawnPosition, SpawnEntity}
import de.lightwave.rooms.engine.mapping.MapCoordinator.GetDoorPosition
import de.lightwave.rooms.engine.mapping.Vector2
import de.lightwave.rooms.repository.RoomRepositorySpec
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

  test("Spawn entity") {
    withActor() { director =>
      director ! Initialize(RoomRepositorySpec.expectedRoom)
      expectMsg(Initialized)

      director ! SpawnEntity(EntityReference(""))
      expectMsgClass(classOf[ActorRef])
    }
  }

  test("Get entity by id") {
    withActor() { director =>
      director ! Initialize(RoomRepositorySpec.expectedRoom)
      expectMsg(Initialized)

      director ! SpawnEntity(EntityReference(""))
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
    assert(director.underlyingActor.spawnPosition == Vector2(1, 1))
  }

  test("Set spawn position to door position on initialization") {
    val coordinatorProbe = TestProbe()
    val director = TestActorRef[EntityDirector](EntityDirector.props()(coordinatorProbe.ref, TestProbe().ref))

    director ! Initialize(RoomRepositorySpec.expectedRoom)
    expectMsg(Initialized)

    coordinatorProbe.expectMsg(GetDoorPosition)
    coordinatorProbe.reply(Vector2(1, 1))

    assert(director.underlyingActor.spawnPosition == Vector2(1, 1))
  }

  private def withActor()(testCode: ActorRef => Any): Unit = {
    testCode(system.actorOf(EntityDirector.props()(TestProbe().ref, TestProbe().ref)))
  }

  override def afterAll: Unit = {
    shutdown()
  }
}
