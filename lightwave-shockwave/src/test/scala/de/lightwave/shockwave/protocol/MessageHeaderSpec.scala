package de.lightwave.shockwave.protocol

import akka.util.ByteString
import org.scalatest.FunSuite

class MessageHeaderSpec extends FunSuite {
  test("Create message header from byte string") {
    assert(MessageHeader.from(ByteString("@@C@A")) == MessageHeader(3, 1))
  }
}
