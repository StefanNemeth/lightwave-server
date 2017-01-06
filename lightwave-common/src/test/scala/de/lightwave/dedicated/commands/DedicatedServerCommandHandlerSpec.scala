package de.lightwave.dedicated.commands

import org.scalatest.FunSuite

class DedicatedServerCommandHandlerSpec extends FunSuite {
  test("Handle command") {
    createHandler().handle("test")
  }

  test("Throw exception on valid command without a context") {
    intercept[IllegalStateException] {
      createHandler(false).handle("test")
    }
  }

  test("Throw exception on invalid command") {
    intercept[IllegalArgumentException] {
      createHandler().handle("")
    }
  }

  test("Throw exception on non-existent command") {
    intercept[IllegalArgumentException] {
      createHandler().handle("abc")
    }
  }

  def createHandler(setContext: Boolean = true): DedicatedServerCommandHandler = {
    val handler = new DedicatedServerCommandHandler

    if (setContext) {
      handler.setContext(new TestDedicatedServerCommandContext())
    }

    handler
  }
}

class TestDedicatedServerCommand extends DedicatedServerCommand {
  override def commandName: String = "test"

  override def execute(args: Array[String]): Unit = {
    // Empty
  }
}

class TestDedicatedServerCommandContext extends DedicatedServerCommandContext {
  override def initialize(): Unit = {
    registerCommand(new TestDedicatedServerCommand())
  }
}
