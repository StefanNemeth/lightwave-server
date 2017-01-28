package de.lightwave.rooms.engine.entities

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.lightwave.rooms.engine.entities.RoomEntity.{SetPosition, TeleportTo}
import de.lightwave.rooms.engine.mapping.MapCoordinator.GetHeight
import de.lightwave.rooms.engine.mapping.{Vector2, Vector3}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class RoomEntitySpec extends TestKit(ActorSystem("test-system", ConfigFactory.empty))
  with FunSuiteLike
  with DefaultTimeout
  with ImplicitSender
  with BeforeAndAfterAll {

  test("Set position") {
    withActor() { (entity, _) =>
      entity ! SetPosition(Vector3(1, 1, 1))
      assert(entity.underlyingActor.position == Vector3(1, 1, 1))
    }
  }

  test("Teleport to position") {
    withActor() { (entity, coordinator) =>
      entity ! TeleportTo(Vector2(1, 1))

      coordinator.expectMsg(GetHeight(1, 1))
      coordinator.reply(Some(2.0))

      assert(entity.underlyingActor.position == Vector3(1, 1, 2))
    }
  }

  private def withActor()(testCode: (TestActorRef[RoomEntity], TestProbe) => Any): Unit = {
    val coordinator = TestProbe()

    testCode(TestActorRef[RoomEntity](RoomEntity.props(1, EntityReference(""))(coordinator.ref, TestProbe().ref)), coordinator)
  }

  override def afterAll: Unit = {
    shutdown()
  }
}
