package de.lightwave.players.helper

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
  * Helper class for generating hashes and salts that are
  * used for safely checking login credentials and registration data
  */
object PasswordHash {
  /** Returns byte array hash created with PBKDF2WithHmacSHA1.
    *
    * @param clearText  Text to be hashed.
    * @param salt       Salt used when hashing the clearText.
    * @return           Hashed clearText.
    */
  def hash(clearText: Array[Char], salt: Array[Byte]): Array[Byte] = {
    // Read about these values for PBKDF2 before changing iterationCount or keyLength.
    // Beware of changing these values if there are existing users in the database (you might break them).
    val iterationCount = 20480
    val keyLength = 160

    val spec = new PBEKeySpec(clearText, salt, iterationCount, keyLength)
    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded
  }

  /** Generate a pseudo-random salt used when hashing passwords created with SHA1PRNG.
    *
    * The salt is considered a non-secret and can be stored with the passwords in the database.
    * When a password is changed, the salt can, but does not have to be modified.
    *
    * @return Pseudo-random generated salt.
    */
  def randomSalt: Array[Byte] = {
    val numberOfBytes = 20
    val random = SecureRandom.getInstance("SHA1PRNG")
    val salt = new Array[Byte](numberOfBytes)
    random.nextBytes(salt)
    salt
  }
}
