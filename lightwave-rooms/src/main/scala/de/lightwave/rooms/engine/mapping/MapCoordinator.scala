package de.lightwave.rooms.engine.mapping

import akka.actor.{ActorLogging, Props}
import de.lightwave.rooms.engine.EngineComponent
import de.lightwave.rooms.engine.EngineComponent.{AlreadyInitialized, Initialize, Initialized}
import de.lightwave.rooms.model.{RoomModel, RoomModels}
import de.lightwave.rooms.repository.RoomModelRepository

/**
  * Represents source of truth regarding all map states
  * and path finding of a room.
  */
class MapCoordinator(modelRepository: RoomModelRepository) extends EngineComponent with ActorLogging {
  // Define maps that are responsible for storing heights
  // and states of all tiles
  var heights = new RoomMap[Int](0, 0)
  var states  = new RoomMap[MapUnit](0, 0)

  def init(model: RoomModel): Unit = {
    log.debug(s"Initializing coordinator using height map '${model.heightMap}'")

    heights = RoomModelParser.toHeightMap(model)
    states = RoomModelParser.toStateMap(model)

    sender() ! Initialized
    context.become(initializedReceive)
  }

  override def receive: Receive = initialReceive

  def initialReceive: Receive = {
    case Initialize(room) => room.modelId match {
      case None => init(RoomModels.DefaultMap)
      case Some(modelId) => modelRepository.getById(modelId) match {
        case None => init(RoomModels.DefaultMap)
        case Some(model) => init(model)
      }
    }
  }

  def initializedReceive: Receive = {
    case Initialize(_) => sender() ! AlreadyInitialized
  }
}

object MapCoordinator {
  def props(modelRepository: RoomModelRepository) = Props(classOf[MapCoordinator], modelRepository)
  def props(): Props = props(RoomModelRepository)
}