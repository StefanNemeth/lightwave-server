package de.lightwave.rooms.engine.entity

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.lightwave.rooms.engine.entity.RoomEntity._
import de.lightwave.rooms.engine.entity.StanceProperty.WalkingTo
import de.lightwave.rooms.engine.mapping.MapCoordinator.{BlockTile, BlockTileTowardsDestination, ClearTile, GetHeight}
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
      coordinator.reply(Some(new Vector3(1, 1, 2)))

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
      entity ! WalkTo(Vector2(1))

      coordinator.expectMsg(BlockTileTowardsDestination(Vector2.empty, Vector2(1)))
      coordinator.reply(Some(Vector3(1)))

      // 1. Start walking animation
      broadcaster.expectMsg(Publish(PositionUpdated(1, Vector3.empty, RoomEntity.DefaultStance.copy(properties = Set(
        WalkingTo(Vector2(1))
      )))))

      // 2. End walking animation
      broadcaster.expectMsg(Publish(PositionUpdated(1, Vector2(1), RoomEntity.DefaultStance)))
      val t1 = System.nanoTime()

      assert((t1 - t0) >= RoomEntity.WalkingSpeed.toNanos)

      // 3. Clear old position
      coordinator.expectMsg(ClearTile(0, 0))
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
