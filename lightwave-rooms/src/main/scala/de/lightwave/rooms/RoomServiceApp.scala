package de.lightwave.rooms

import akka.actor.ActorSystem
import de.lightwave.services.ServiceApp

object RoomServiceApp extends ServiceApp {
  override def onStart(system: ActorSystem) = {
    system.actorOf(RoomService.props(), name = RoomService.ServiceName)
  }
}
