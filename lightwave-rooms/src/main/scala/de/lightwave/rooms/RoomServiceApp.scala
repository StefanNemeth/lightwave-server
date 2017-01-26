package de.lightwave.rooms

import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.config.Config
import de.lightwave.dedicated.commands.{DedicatedServerCommandContext, DedicatedServerCommandHandler}
import de.lightwave.rooms.engine.RoomEngine.InitializeRoom
import de.lightwave.rooms.model.Room
import de.lightwave.services.ServiceApp

object DedicatedRoomServiceCommandContext extends DedicatedServerCommandContext {
  override def handle(args: Array[String]) = {
    case "help" => write("Available commands:")
  }
}

object RoomServiceApp extends ServiceApp {
  val ServiceName = "roomService"

  override def onStart(config: Config, system: ActorSystem, commandHandler: DedicatedServerCommandHandler) = {
    system.actorOf(RoomService.props(), ServiceName)

    commandHandler.setContext(DedicatedRoomServiceCommandContext)
  }
}