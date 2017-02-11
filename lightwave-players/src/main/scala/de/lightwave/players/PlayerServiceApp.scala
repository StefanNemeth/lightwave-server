package de.lightwave.players

import akka.actor.ActorSystem
import com.typesafe.config.Config
import de.lightwave.dedicated.commands.DedicatedServerCommandHandler
import de.lightwave.services.ServiceApp

object PlayerServiceApp extends ServiceApp {
  val serviceName = "PlayerService"
  val role = Some("players")

  override def onStart(config: Config, system: ActorSystem, commandHandler: DedicatedServerCommandHandler) = {
    system.actorOf(PlayerService.props(), serviceName)
  }
}
