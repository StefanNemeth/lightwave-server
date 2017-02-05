package de.lightwave.shockwave.connection

import java.net.InetSocketAddress

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActorRef, TestKit, TestProbe}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import de.lightwave.shockwave.connection.ConnectionHandler.{ParseMessage, ParsedMessage}
import de.lightwave.shockwave.protocol.MessageHeader
import org.scalatest.{BeforeAndAfterAll, FunSuite, FunSuiteLike}

class ConnectionHandlerSpec extends TestKit(ActorSystem("test-system", ConfigFactory.empty))
  with FunSuiteLike
  with DefaultTimeout
  with ImplicitSender
  with BeforeAndAfterAll {

  test ("Parse message in a single chunk") {
    withActor() { handler =>
      handler ! ParseMessage(ByteString("@@@@A"))
      expectMsg(ParsedMessage(MessageHeader(0, 1), ByteString("")))
    }
  }

  test ("Parse message in multiple chunks") {
    withActor() { handler =>
      handler ! ParseMessage(ByteString("@@C@B"))
      expectNoMsg()

      handler ! ParseMessage(ByteString("AB"))
      expectNoMsg()

      handler ! ParseMessage(ByteString("C"))
      expectMsg(ParsedMessage(MessageHeader(3, 2), ByteString("ABC")))
    }
  }

  private def withActor()(testCode: (TestActorRef[ConnectionHandler]) => Any) = {
    testCode(TestActorRef[ConnectionHandler](
      ConnectionHandler.props(new InetSocketAddress("127.0.0.1", 8123), TestProbe().ref)
    ))
  }

  override def afterAll: Unit = {
    shutdown()
  }
}
