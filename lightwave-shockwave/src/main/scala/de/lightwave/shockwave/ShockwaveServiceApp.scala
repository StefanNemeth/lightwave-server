package de.lightwave.shockwave

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import com.typesafe.config.Config
import de.lightwave.dedicated.commands.DedicatedServerCommandHandler
import de.lightwave.services.ServiceApp

object ShockwaveServiceApp extends ServiceApp {
  val ConfigEndpointHostPath = "lightwave.shockwave.endpoint.host"
  val ConfigEndpointPortPath = "lightwave.shockwave.endpoint.port"

  override def onStart(config: Config, system: ActorSystem, commandHandler: DedicatedServerCommandHandler): Unit = {
    system.actorOf(ShockwaveService.props(
      new InetSocketAddress(config.getString(ConfigEndpointHostPath), config.getInt(ConfigEndpointPortPath))
    ), "ShockwaveService")
  }
}
