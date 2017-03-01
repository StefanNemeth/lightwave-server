package de.lightwave.rooms.engine.entity

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.lightwave.rooms.engine.entity.RoomEntity._
import de.lightwave.rooms.engine.entity.StanceProperty.WalkingTo
import de.lightwave.rooms.engine.mapping.MapCoordinator.GetHeight
import de.lightwave.rooms.engine.mapping.{Vector2, Vector3}
import de.lightwave.services.pubsub.Broadcaster.Publish
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class RoomEntitySpec extends TestKit(ActorSystem("test-system", ConfigFactory.empty))
  with FunSuiteLike
  with DefaultTimeout
  with ImplicitSender
  with BeforeAndAfterAll {

  import scala.concurrent.duration._

  test("Set position") {
    withActor() { (entity, _, _) =>
      entity ! SetPosition(Vector3(1, 1, 1))
      assert(entity.underlyingActor.position == Vector3(1, 1, 1))
    }
  }

  test("Broadcast new position") {
    withActor() { (entity, _, broadcaster) =>
      entity ! SetPosition(Vector3(1, 1, 1))
      broadcaster.expectMsg(Publish(PositionUpdated(1, Vector3(1, 1, 1), RoomEntity.DefaultStance)))
    }
  }

  test("Teleport to position") {
    withActor() { (entity, coordinator, _) =>
      entity ! TeleportTo(Vector2(1, 1))

      coordinator.expectMsg(GetHeight(1, 1))
      coordinator.reply(Some(2.0))

      assert(entity.underlyingActor.position == Vector3(1, 1, 2))
    }
  }

  test("Get position") {
    withActor() { (entity, _, _) =>
      entity ! SetPosition(Vector3(1, 1, 1))

      entity ! GetPosition
      expectMsg(Vector3(1, 1, 1))
    }
  }

  test("Get render information") {
    withActor() { (entity, _, _) =>
      entity ! GetRenderInformation
      expectMsg(RenderInformation(1, EntityReference(1, ""), Vector3.empty, RoomEntity.DefaultStance))
    }
  }

  test("Walk to tile") {
    withActor() { (entity, coordinator, broadcaster) =>
      val t0 = System.nanoTime()
      entity ! WalkTo(new Vector2(1, 0))

      // 1. Start walking animation
      broadcaster.expectMsg(Publish(PositionUpdated(1, Vector3.empty, RoomEntity.DefaultStance.copy(properties = Seq(
        WalkingTo(new Vector2(1, 0))
      )))))

      // 2. End walking animation
      broadcaster.expectMsg(Publish(PositionUpdated(1, new Vector2(1, 0), RoomEntity.DefaultStance)))
      val t1 = System.nanoTime()

      // Wait at least 500 milliseconds
      assert((t1 - t0) >= RoomEntity.WalkingSpeed.toNanos)
    }
  }

  private def withActor()(testCode: (TestActorRef[RoomEntity], TestProbe, TestProbe) => Any): Unit = {
    val coordinator = TestProbe()
    val broadcaster = TestProbe()

    testCode(TestActorRef[RoomEntity](RoomEntity.props(
      1, EntityReference(1, ""))(coordinator.ref, broadcaster.ref)
    ), coordinator, broadcaster)
  }

  override def afterAll: Unit = {
    shutdown()
  }
}
