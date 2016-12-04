package de.lightwave.rooms.engine.mapping

import akka.actor.Props
import de.lightwave.rooms.engine.EngineComponent
import de.lightwave.rooms.engine.EngineComponent.Initialize
import de.lightwave.rooms.repository.RoomModelRepository

class MapCoordinator(repository: RoomModelRepository) extends EngineComponent {
  override def receive: Receive = {
    case Initialize(room) =>
  }
}

object MapCoordinator {
  // Use Postgres repository by default
  def props(): Props = props(RoomModelRepository)

  def props(repository: RoomModelRepository) = Props(classOf[MapCoordinator], repository)
}