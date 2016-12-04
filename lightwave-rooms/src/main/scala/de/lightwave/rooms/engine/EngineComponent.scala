package de.lightwave.rooms.engine

import akka.actor.{Actor, Props}
import de.lightwave.rooms.model.Room

/**
  * Represents a basic component of a room engine that is responsible for a
  * specific functionality (e.g. items, players, games)
  */
trait EngineComponent extends Actor

object EngineComponent {
  def props(component: Class[EngineComponent]) = Props(component)

  case class Initialize(room: Room)
}
