package de.lightwave.shockwave

import java.net.InetSocketAddress

import akka.actor.ActorSystem
import com.typesafe.config.Config
import de.lightwave.dedicated.commands.DedicatedServerCommandHandler
import de.lightwave.services.ServiceApp

object ShockwaveServiceApp extends ServiceApp {
  override def onStart(config: Config, system: ActorSystem, commandHandler: DedicatedServerCommandHandler): Unit = {
    system.actorOf(ShockwaveService.props(new InetSocketAddress("127.0.0.1", 30000)), "ShockwaveService")
  }
}
