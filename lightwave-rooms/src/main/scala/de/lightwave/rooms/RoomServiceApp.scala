package de.lightwave.rooms

import akka.actor.{ActorRef, ActorSystem}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import com.typesafe.config.Config
import de.lightwave.dedicated.commands.{DedicatedServerCommandContext, DedicatedServerCommandHandler}
import de.lightwave.rooms.engine.RoomEngine
import de.lightwave.rooms.engine.RoomEngine.InitializeRoom
import de.lightwave.rooms.engine.entities.RoomEntity
import de.lightwave.rooms.model.Room
import de.lightwave.services.ServiceApp

object DedicatedRoomServiceCommandContext extends DedicatedServerCommandContext {
  override def handle(args: Array[String]) = {
    case "help" => write("Available commands:")
  }
}

object RoomServiceApp extends ServiceApp {
  val serviceName = "roomService"
  val role = Some("rooms")

  override def onStart(config: Config, system: ActorSystem, commandHandler: DedicatedServerCommandHandler) = {
    // Hosts room engines on service (access them via "startProxy")
    ClusterSharding(system).start(
      typeName = RoomEngine.shardName,
      entityProps = RoomEngine.props(),
      settings = ClusterShardingSettings(system).withRole(role),
      extractEntityId = RoomEngine.extractEntityId,
      extractShardId = RoomEngine.extractShardId)

    // Provides room data
    system.actorOf(RoomService.props(), serviceName)

    commandHandler.setContext(DedicatedRoomServiceCommandContext)
  }
}