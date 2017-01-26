package de.lightwave.dedicated.commands

import com.typesafe.scalalogging.Logger


trait DedicatedServerCommandContext {
  def handle(args: Array[String]): PartialFunction[Any, Unit]

  // TODO: Reimplement
  // Write response back to sender
  def write(rep: String): Unit = {
    DedicatedServerCommandContext.Log.info(rep)
  }
}

object DedicatedServerCommandContext {
  val Log = Logger("server")
}