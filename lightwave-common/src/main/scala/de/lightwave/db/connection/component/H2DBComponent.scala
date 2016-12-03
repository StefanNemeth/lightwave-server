package de.lightwave.db.connection.component

import de.lightwave.db.connection.DBComponent

trait H2DBComponent extends DBComponent {
  override val driver = slick.driver.H2Driver
  override val db = H2DBDatabase.db
}

private[component] object H2DBDatabase {
  import slick.driver.H2Driver.api._

  val db = Database.forConfig("lightwave.db.h2db")
}
