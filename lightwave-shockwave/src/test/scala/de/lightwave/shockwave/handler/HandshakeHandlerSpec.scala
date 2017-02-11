package de.lightwave.shockwave.handler

import akka.actor.{ActorRef, ActorSystem}
import akka.io.Tcp.Write
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import de.lightwave.shockwave.handler.HandshakeHandler
import de.lightwave.shockwave.io.protocol.messages.{GenerateKeyMessage, InitCryptoMessage, InitCryptoMessageComposer, SessionParamsMessageComposer}
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class HandshakeHandlerSpec extends TestKit(ActorSystem("test-system", ConfigFactory.empty))
  with FunSuiteLike
  with DefaultTimeout
  with ImplicitSender
  with BeforeAndAfterAll {

  test("Send crypto settings on init crypto message") {
    withActor() { handler =>
      handler ! InitCryptoMessage
      expectMsg(Write(InitCryptoMessageComposer.compose()))
    }
  }

  test("Send session parameters instead of secret key on generate key message") {
    withActor() { handler =>
      handler ! GenerateKeyMessage
      expectMsg(Write(SessionParamsMessageComposer.compose()))
    }
  }

  private def withActor()(testCode: ActorRef => Any) = {
    testCode(system.actorOf(HandshakeHandler.props()))
  }
}
