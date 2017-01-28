package de.lightwave.console

import akka.actor.ActorSystem
import akka.cluster.sharding.ClusterSharding
import com.typesafe.config.Config
import de.lightwave.dedicated.commands.DedicatedServerCommandHandler
import de.lightwave.rooms.RoomServiceApp
import de.lightwave.rooms.engine.RoomEngine
import de.lightwave.services.{ServiceApp, ServiceGroups}

import scala.concurrent.Future

object ConsoleApp extends ServiceApp {
  override def onStart(config: Config, system: ActorSystem, commandHandler: DedicatedServerCommandHandler): Unit = {
    val roomRegion = ClusterSharding(system).startProxy(
      typeName = RoomEngine.shardName,
      role = Some("rooms"),
      extractEntityId = RoomEngine.extractEntityId,
      extractShardId = RoomEngine.extractShardId)

    val roomService = ServiceGroups.createGroup(system, RoomServiceApp.ServiceName, Some("rooms"))

    system.actorOf(ConsoleService.props(commandHandler, roomService, roomRegion))
  }
}
