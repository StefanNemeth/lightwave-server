package de.lightwave.shockwave.io.protocol

import akka.util.ByteString
import de.lightwave.shockwave.io.protocol.message.MessageHeader
import org.scalatest.FunSuite

class MessageReaderSpec extends FunSuite {
  test("Get remaining bytes") {
    assert(initReader("@@").remainingBytes == 2)
  }

  test("Reset pointer") {
    val reader = initReader("@@")
    reader.readByte
    assert(reader.pointer == 1)
    reader.reset()
    assert(reader.pointer == 0)
  }

  test("Read integer") {
    assert(initReader("I").readInt == 1)
    assert(initReader("").readInt == 0)
    assert(initReader("p@JkyN").readInt == 0) // Trigger error
  }

  test("Read short") {
    assert(initReader("@A").readShort == 1)
    assert(initReader("").readShort == 0)
  }

  test("Read string") {
    assert(initReader("@Dtest").readString == "test")
    assert(initReader("@Etest").readString == "")
  }

  test("Read boolean") {
    assert(initReader("I").readBoolean === true)
    assert(initReader("H").readBoolean === false)
  }

  test("Read byte") {
    assert(initReader("A").readByte == 'A'.toByte)
    assert(initReader("").readByte == 0.toByte)
  }

  private def initReader(str: String) = new MessageReader(MessageHeader(0, 0), ByteString.fromString(str))
}
