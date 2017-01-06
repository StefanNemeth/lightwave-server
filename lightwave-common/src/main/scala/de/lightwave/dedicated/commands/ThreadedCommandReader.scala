package de.lightwave.dedicated.commands

import scala.io.StdIn

class ThreadedCommandReader(name: String, private val commandHandler: DedicatedServerCommandHandler) extends Thread(name) {
  override def run(): Unit = Iterator.continually(StdIn.readLine).takeWhile(_ != "stop").foreach(line => {
    try {
      if (!line.isEmpty)
        commandHandler.handle(line)
    } catch {
      case ex: Exception => DedicatedServerCommandHandler.Log.error(ex.getMessage)
    }
  })
}
