package de.lightwave.players.helper

import org.scalatest.FunSuite

class PasswordHashSpec extends FunSuite {
  test("Get random salt") {
    val salt1 = PasswordHash.randomSalt
    val salt2 = PasswordHash.randomSalt

    assert(salt1.length == 20 && salt2.length == 20 && !salt1.sameElements(salt2))
  }

  test("Hash plain text") {
    assert(PasswordHash.hash("test".toCharArray, PasswordHashSpec.hexString2ByteArray("121F3EA5F4E9CC2AA5C097851E7767682AAD1C06")) === PasswordHashSpec.hexString2ByteArray("C6BE0D6DFC70198BF88E36520B8FC4A4DCB6353D"))
  }
}

object PasswordHashSpec {
  def hexString2ByteArray(hex: String): Array[Byte] = {
    hex.replaceAll("[^0-9A-Fa-f]", "").sliding(2, 2).toArray.map(Integer.parseInt(_, 16).toByte)
  }
}