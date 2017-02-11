package de.lightwave.players

import akka.actor.ActorSystem
import com.typesafe.config.Config
import de.lightwave.dedicated.commands.DedicatedServerCommandHandler
import de.lightwave.services.ServiceApp

object PlayerServiceApp extends ServiceApp {
  val ServiceName = "PlayerService"

  override def onStart(config: Config, system: ActorSystem, commandHandler: DedicatedServerCommandHandler) = {

  }
}
