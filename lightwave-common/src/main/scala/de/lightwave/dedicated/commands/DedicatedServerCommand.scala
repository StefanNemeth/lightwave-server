package de.lightwave.dedicated.commands

trait DedicatedServerCommand {
  def commandName: String
  def execute(args: Array[String]): Unit
}
