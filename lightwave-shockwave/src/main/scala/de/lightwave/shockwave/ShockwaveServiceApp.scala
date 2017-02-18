package de.lightwave.shockwave

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorSystem}
import akka.cluster.sharding.ClusterSharding
import com.typesafe.config.Config
import de.lightwave.dedicated.commands.DedicatedServerCommandHandler
import de.lightwave.players.PlayerService.GetPlayer
import de.lightwave.players.PlayerServiceApp
import de.lightwave.rooms.RoomServiceApp
import de.lightwave.rooms.engine.RoomEngine
import de.lightwave.services.{ServiceApp, ServiceGroups}

object ShockwaveServiceApp extends ServiceApp {
  val ConfigEndpointHostPath = "lightwave.shockwave.endpoint.host"
  val ConfigEndpointPortPath = "lightwave.shockwave.endpoint.port"

  val serviceName = "ShockwaveService"
  val role = Some("shockwave")

  override def onStart(config: Config, system: ActorSystem, commandHandler: DedicatedServerCommandHandler): Unit = {
    val roomRegion = ClusterSharding(system).startProxy(
      typeName = RoomEngine.shardName,
      role = RoomServiceApp.role,
      extractEntityId = RoomEngine.extractEntityId,
      extractShardId = RoomEngine.extractShardId)

    val playerService = ServiceGroups.createGroup(system, PlayerServiceApp)

    system.actorOf(ShockwaveService.props(
      new InetSocketAddress(config.getString(ConfigEndpointHostPath), config.getInt(ConfigEndpointPortPath)),
      playerService,
      roomRegion
    ), serviceName)
  }
}