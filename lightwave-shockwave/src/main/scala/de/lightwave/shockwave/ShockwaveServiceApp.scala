package de.lightwave.shockwave

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import com.typesafe.config.Config
import de.lightwave.dedicated.commands.DedicatedServerCommandHandler
import de.lightwave.players.PlayerServiceApp
import de.lightwave.services.{ServiceApp, ServiceGroups}

object ShockwaveServiceApp extends ServiceApp {
  val ConfigEndpointHostPath = "lightwave.shockwave.endpoint.host"
  val ConfigEndpointPortPath = "lightwave.shockwave.endpoint.port"

  val serviceName = "ShockwaveService"
  val role = Some("shockwave")

  override def onStart(config: Config, system: ActorSystem, commandHandler: DedicatedServerCommandHandler): Unit = {
    system.actorOf(ShockwaveService.props(
      new InetSocketAddress(config.getString(ConfigEndpointHostPath), config.getInt(ConfigEndpointPortPath)),
      ServiceGroups.createGroup(system, PlayerServiceApp)
    ), serviceName)
  }
}
