package de.lightwave.dedicated.commands

import scala.collection.mutable

abstract class DedicatedServerCommandContext {
  private val commands = mutable.HashMap.empty[String, DedicatedServerCommand]

  initialize()

  def registerCommand(command: DedicatedServerCommand): Unit = {
    commands.put(command.commandName, command)
  }

  def getCommand(name: String): Option[DedicatedServerCommand] = commands.get(name)

  def initialize(): Unit
}
