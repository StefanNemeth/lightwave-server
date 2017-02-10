package de.lightwave.shockwave.io.protocol

import akka.util.ByteString
import org.scalatest.FunSuite

class ShockwaveMessageWriterSpec extends FunSuite {
  test("Get byte string") {
    assert(new ShockwaveMessageWriter(1).toByteString == ByteString("@A" + 1.toChar))
  }

  test("Push short") {
    assert(new ShockwaveMessageWriter(0).push(1.toShort).toByteString == ByteString("@@@A" + 1.toChar))
    assert(new ShockwaveMessageWriter(0).push((-1).toShort).toByteString == ByteString("@@@@" + 1.toChar))
  }

  test("Push integer") {
    assert(new ShockwaveMessageWriter(0).push(1).toByteString == ByteString("@@I" + 1.toChar))
  }

  test("Push boolean") {
    assert(new ShockwaveMessageWriter(0).push(true).toByteString == ByteString("@@I" + 1.toChar))
    assert(new ShockwaveMessageWriter(0).push(false).toByteString == ByteString("@@H" + 1.toChar))
  }

  test("Push string") {
    assert(new ShockwaveMessageWriter(0).push("test").toByteString == ByteString("@@test" + 2.toChar + 1.toChar))
  }

  test("Filter string") {
    assert(ShockwaveMessageWriter.filterString(2.toChar + "hi") == "hi")
  }
}
