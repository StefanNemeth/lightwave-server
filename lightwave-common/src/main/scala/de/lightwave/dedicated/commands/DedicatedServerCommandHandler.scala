package de.lightwave.dedicated.commands

import com.typesafe.scalalogging.Logger
import DedicatedServerCommandHandler.Log

class DedicatedServerCommandHandler {
  private var context: Option[DedicatedServerCommandContext] = None

  def setContext(newContext: DedicatedServerCommandContext): Unit =
    context = Some(newContext)

  /**
    * Parse a command and execute it on the current context
    * @throws IllegalStateException or IllegalArgumentException on
    *         - Non-existent commands
    *         - Unset context
    *         - Invalid commands (empty line)
    */
  def handle(commandLine: String): Unit = context match {
    case None => throw new IllegalStateException("No context set")
    case Some(extractedContext) => {
      val parts = commandLine.split(" ")

      if (parts.length < 1 || parts(0).length < 1) {
        throw new IllegalArgumentException("Invalid command (empty)")
      }

      extractedContext.getCommand(parts(0).toLowerCase()) match {
        case None => throw new IllegalArgumentException("Command not found")
        case Some(command) =>
          val args = Array.ofDim[String](parts.length - 1)
          System.arraycopy(parts, 1, args, 0, parts.length - 1)
          command.execute(args)
      }
    }
  }
}

object DedicatedServerCommandHandler {
  val Log = Logger[DedicatedServerCommandHandler]
}