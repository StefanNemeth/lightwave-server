package de.lightwave.db.connection.component

import java.util.UUID

import de.lightwave.db.connection.DBComponent

/**
  * Used for testing purposes
  * Caution: Don't use it in production!
  */
class TestH2DBComponent(scripts: Array[String]) extends DBComponent {
  override val driver = slick.driver.H2Driver

  import driver.api._

  val randomDB = "jdbc:h2:mem:test" + UUID.randomUUID().toString() + ";"

  val h2Url = randomDB + "MODE=PostgreSQL;DATABASE_TO_UPPER=false;INIT=" +
    scripts.map(script => s"runscript from '${script.replaceAll("'", "\\'")}'").mkString("\\;")

  override val db = Database.forURL(url = h2Url, driver = "org.h2.Driver")
}
