package de.lightwave.rooms.engine.entities

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import de.lightwave.rooms.engine.EngineComponent.{AlreadyInitialized, Initialize, Initialized}
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

  private def withActor()(testCode: ActorRef => Any): Unit = {
    testCode(system.actorOf(EntityDirector.props()))
  }

  override def afterAll = {
    shutdown()
  }
}
