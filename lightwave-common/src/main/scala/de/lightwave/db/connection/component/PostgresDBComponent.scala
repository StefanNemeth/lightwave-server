package de.lightwave.db.connection.component

import de.lightwave.db.connection.DBComponent

/**
  * Postgres is used in production environments for storing data such as
  * rooms, players and the catalogue
  */
trait PostgresDBComponent extends DBComponent {
  override val driver = slick.driver.PostgresDriver
  override val db = PostgresDatabase.db
}

private[component] object PostgresDatabase {
  import slick.driver.PostgresDriver.api._

  val db = Database.forConfig("lightwave.db.postgres")
}