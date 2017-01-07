package de.lightwave.dedicated.commands


trait DedicatedServerCommandContext {
  def handle(args: Array[String]): PartialFunction[Any, Unit]
}