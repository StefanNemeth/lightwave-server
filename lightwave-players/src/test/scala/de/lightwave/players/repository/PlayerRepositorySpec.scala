package de.lightwave.players.repository

import de.lightwave.db.connection.component.TestH2DBComponent
import de.lightwave.players.helper.PasswordHashSpec
import de.lightwave.players.model.{Player, SecurePlayer}
import org.scalatest.FunSuite
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}

class PlayerRepositorySpec extends FunSuite with ScalaFutures {
  implicit val defaultPatience = PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  val repository: PlayerRepository = PlayerRepositorySpec.getRepository

  test("Get player by id") {
    val player = repository.getById(PlayerRepositorySpec.expectedPlayer.id.get)

    whenReady(player) { player =>
      assert(player.isDefined && player.get.password.sameElements(PlayerRepositorySpec.expectedPlayer.password) && player.get.salt.sameElements(PlayerRepositorySpec.expectedPlayer.salt))
      assert(player.get.copy(password = PlayerRepositorySpec.expectedPlayer.password, salt = PlayerRepositorySpec.expectedPlayer.salt) === PlayerRepositorySpec.expectedPlayer)
    }
  }

  test("Get player by nickname") {
    val player = repository.getByNickname(PlayerRepositorySpec.expectedPlayer.nickname)

    whenReady(player) { player =>
      assert(player.isDefined && player.get.id === PlayerRepositorySpec.expectedPlayer.id)
    }
  }
}

object PlayerRepositorySpec {
  def getRepository = new TestH2DBComponent(Array(
    "lightwave-players/src/test/resources/db/schema.sql", "lightwave-players/src/test/resources/db/schemadata.sql"
  )) with PlayerRepository

  val expectedPlayer = SecurePlayer(Some(1), "Steve", PasswordHashSpec.hexString2ByteArray("C6BE0D6DFC70198BF88E36520B8FC4A4DCB6353D"), PasswordHashSpec.hexString2ByteArray("121F3EA5F4E9CC2AA5C097851E7767682AAD1C06"))
}