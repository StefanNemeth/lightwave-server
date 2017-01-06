package de.lightwave.rooms

import akka.actor.ActorSystem
import com.typesafe.config.Config
import de.lightwave.dedicated.commands.DedicatedServerCommandHandler
import de.lightwave.services.ServiceApp

object RoomServiceApp extends ServiceApp {
  val ServiceName = "roomService"

  override def onStart(config: Config, system: ActorSystem, commandHandler: DedicatedServerCommandHandler) = {
    system.actorOf(RoomService.props(), ServiceName)
  }
}
