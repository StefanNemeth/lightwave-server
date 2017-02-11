package de.lightwave.shockwave.handler

import akka.actor.{ActorRef, ActorSystem}
import akka.io.Tcp.Write
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit, TestProbe}
import com.typesafe.config.ConfigFactory
import de.lightwave.players.PlayerService.AuthenticatePlayer
import de.lightwave.players.model.Player
import de.lightwave.shockwave.handler.FrontpageHandler
import de.lightwave.shockwave.io.ShockwaveConnectionHandler.SetPlayerInformation
import de.lightwave.shockwave.io.protocol.messages.{AuthenticatedMessageComposer, LoginFailedMessageComposer, LoginMessage}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class FrontpageHandlerSpec extends TestKit(ActorSystem("test-system", ConfigFactory.empty))
  with FunSuiteLike
  with DefaultTimeout
  with ImplicitSender
  with BeforeAndAfterAll {

  test("Login using valid credentials") {
    withActor() { (handler, playerService) =>
      handler ! LoginMessage("test", "test")

      playerService.expectMsg(AuthenticatePlayer("test", "test"))
      playerService.reply(Some(Player(None, "test")))

      expectMsg(Write(AuthenticatedMessageComposer.compose))
      expectMsg(SetPlayerInformation(Player(None, "test")))
    }
  }

  test("Send error message when logging in using invalid credentials") {
    withActor() { (handler, playerService) =>
      handler ! LoginMessage("test", "test")

      playerService.expectMsg(AuthenticatePlayer("test", "test"))
      playerService.reply(None)

      expectMsg(Write(LoginFailedMessageComposer.compose("Login Incorrect: Invalid username/password combination.")))
    }
  }

  private def withActor()(testCode: (ActorRef, TestProbe) => Any) = {
    val playerService = TestProbe()

    testCode(system.actorOf(FrontpageHandler.props(playerService.ref)), playerService)
  }
}
