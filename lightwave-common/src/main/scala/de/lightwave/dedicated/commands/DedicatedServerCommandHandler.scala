package de.lightwave.dedicated.commands

import com.typesafe.scalalogging.Logger
import DedicatedServerCommandHandler.Log

class DedicatedServerCommandHandler {
  private var context: Option[DedicatedServerCommandContext] = None

  def setContext(newContext: DedicatedServerCommandContext): Unit =
    context = Some(newContext)

  /**
    * Parse a command and execute it on the current context
    * @throws IllegalStateException on unset context
    *         IllegalArgumentException on invalid input
    *         MatchError on non-existent commands
    */
  def handle(commandLine: String): Unit = context match {
    case None => throw new IllegalStateException("No context set")
    case Some(extractedContext) => {
      val parts = commandLine.split(" ")

      if (parts.length < 1 || parts(0).length < 1) {
        throw new IllegalArgumentException("Invalid command (empty)")
      }

      val args = Array.ofDim[String](parts.length - 1)
      System.arraycopy(parts, 1, args, 0, parts.length - 1)

      try {
        extractedContext.handle(args)(parts(0).toLowerCase())
      } catch {
        case ex: MatchError => throw new MatchError("Command doesn't exist")
      }
    }
  }
}

object DedicatedServerCommandHandler {
  val Log = Logger[DedicatedServerCommandHandler]
}