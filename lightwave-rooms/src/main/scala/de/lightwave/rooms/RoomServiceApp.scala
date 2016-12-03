package de.lightwave.rooms

import akka.actor.ActorSystem
import com.typesafe.config.Config
import de.lightwave.services.ServiceApp

object RoomServiceApp extends ServiceApp {
  val ServiceName = "roomService"

  override def onStart(config: Config, system: ActorSystem) = {
    system.actorOf(RoomService.props(), ServiceName)
  }
}
