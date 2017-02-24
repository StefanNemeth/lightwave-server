package de.lightwave.shockwave.handler

import akka.actor.{ActorRef, ActorSystem}
import akka.io.Tcp.Write
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.lightwave.migration.ShockwaveMigration
import de.lightwave.rooms.engine.entities.EntityDirector.SpawnEntity
import de.lightwave.rooms.engine.entities.{EntityReference, EntityStance, RoomEntity}
import de.lightwave.rooms.engine.mapping.MapCoordinator.GetAbsoluteHeightMap
import de.lightwave.rooms.engine.mapping.Vector3
import de.lightwave.services.pubsub.Broadcaster.Subscribe
import de.lightwave.shockwave.io.protocol.messages.{GetUserStancesMessage, _}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class RoomHandlerSpec extends TestKit(ActorSystem("test-system", ConfigFactory.empty))
  with FunSuiteLike
  with DefaultTimeout
  with ImplicitSender
  with BeforeAndAfterAll {

  test("Spawn entity") {
    withActor(handleStartup = false) { (handler, connection, engine) =>
      engine.expectMsg(SpawnEntity(EntityReference(1, "Steve")))
    }
  }

  test("Subscribe to broadcaster of room") {
    withActor(handleStartup = false) { (handler, connection, engine) =>
      engine.expectMsg(SpawnEntity(EntityReference(1, "Steve")))
      engine.expectMsg(Subscribe(handler))
    }
  }

  test("Get heightmap") {
    withActor() { (handler, connection, engine) =>
      val map = IndexedSeq(IndexedSeq(None))
      handler ! GetHeightmapMessage

      engine.expectMsg(GetAbsoluteHeightMap)
      engine.reply(map)

      connection.expectMsg(Write(HeightmapMessageComposer.compose(map)))
    }
  }

  test("Render all entities") {
    withActor() { (handler, connection, engine) =>
      val renderInfo = RoomEntity.RenderInformation(1, EntityReference(1, "Steve"), new Vector3(1, 1, 0), EntityStance(2, 2))
      handler ! GetUsersMessage

      // Placeholder list (required by client)
      connection.expectMsg(Write(EntityListMessageComposer.compose(Seq.empty)))

      engine.expectMsg(RoomEntity.GetRenderInformation)
      engine.reply(renderInfo)

      connection.expectMsg(Write(EntityListMessageComposer.compose(
        Seq((renderInfo.virtualId, renderInfo.reference, renderInfo.position))
      ) ++ EntityStanceMessageComposer.compose(renderInfo.virtualId, renderInfo.position, renderInfo.stance)))
    }
  }

  test("Get test objects") {
    withActor() { (handler, connection, _) =>
      handler ! GetObjectsMessage
      connection.expectMsg(Write(PublicObjectsMessageComposer.compose()))
      connection.expectMsg(Write(FloorItemsMessageComposer.compose()))
    }
  }

  test("Get test wall items") {
    withActor() { (handler, connection, _) =>
      handler ! GetItemsMessage
      connection.expectMsg(Write(WallItemsMessageComposer.compose()))
    }
  }

  test("Display new entity on spawn event") {
    withActor() { (handler, connection, _) =>
      handler ! RoomEntity.Spawned(1, EntityReference(1, "Steve"), TestProbe().ref)
      connection.expectMsg(Write(EntityListMessageComposer.compose(Seq((1, EntityReference(1, "Steve"), new Vector3(0, 0, 0))))))
    }
  }

  test("Update entity stance on position update event") {
    withActor() { (handler, connection, _) =>
      handler ! RoomEntity.PositionUpdated(1, new Vector3(1, 1, 1), EntityStance(2, 2))
      connection.expectMsg(Write(EntityStanceMessageComposer.compose(1, new Vector3(1, 1, 1), EntityStance(2, 2))))
    }
  }

  private def withActor(handleStartup: Boolean = true)(testCode: (ActorRef, TestProbe, TestProbe) => Any) = {
    val connection = TestProbe()
    val roomEngine = TestProbe()

    val handler = system.actorOf(RoomHandler.props(connection.ref, roomEngine.ref))

    if (handleStartup) {
      roomEngine.expectMsg(SpawnEntity(EntityReference(1, "Steve")))
      roomEngine.expectMsg(Subscribe(handler))
    }

    testCode(handler, connection, roomEngine)
  }
}
