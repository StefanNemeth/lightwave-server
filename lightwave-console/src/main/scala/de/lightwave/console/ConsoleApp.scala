package de.lightwave.console

import akka.actor.ActorSystem
import com.typesafe.config.Config
import de.lightwave.dedicated.commands.DedicatedServerCommandHandler
import de.lightwave.services.ServiceApp

object ConsoleApp extends ServiceApp {
  override def onStart(config: Config, system: ActorSystem, commandHandler: DedicatedServerCommandHandler): Unit = {

  }
}
