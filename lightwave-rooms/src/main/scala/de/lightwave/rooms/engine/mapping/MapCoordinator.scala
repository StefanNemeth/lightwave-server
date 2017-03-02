package de.lightwave.rooms.engine.mapping

import akka.actor.{ActorLogging, Props}
import de.lightwave.rooms.engine.EngineComponent
import de.lightwave.rooms.engine.EngineComponent.{AlreadyInitialized, Initialize, Initialized}
import de.lightwave.rooms.engine.mapping.MapCoordinator._
import de.lightwave.rooms.engine.mapping.RoomMap.StaticMap
import de.lightwave.rooms.model.{RoomModel, RoomModels}
import de.lightwave.rooms.repository.RoomModelRepository

/**
  * Represents source of truth regarding all map states
  * and path finding of a room.
  */
class MapCoordinator(modelRepository: RoomModelRepository) extends EngineComponent with ActorLogging {
  // Define maps that are responsible for storing heights
  // and states of all tiles
  var heights = new RoomMap[Double](0, 0)
  var states  = new RoomMap[MapUnit](0, 0)

  // Let's call them pseudo-cache
  var doorPosition = new Vector2(0, 0)
  var absoluteHeightMap: StaticMap[Double] = IndexedSeq[IndexedSeq[Option[Double]]]()

  def initialize(model: RoomModel): Unit = {
    // Dynamic properties
    heights = RoomModelParser.toHeightMap(model)
    states = RoomModelParser.toStateMap(model)

    // "Static" properties
    doorPosition = Vector2.from(model.doorPosition)
    absoluteHeightMap = heights.toStatic

    context.become(initializedReceive)
  }

  override def receive: Receive = initialReceive

  def initialReceive: Receive = {
    case Initialize(room) => room.modelId match {
      case None =>
        initialize(RoomModels.DefaultMap)
        sender() ! InitializedFallback
      case Some(modelId) => modelRepository.getById(modelId) match {
        case None =>
          initialize(RoomModels.DefaultMap)
          sender() ! InitializedFallback
        case Some(model) =>
          initialize(model)
          sender() ! Initialized
      }
    }
  }

  def initializedReceive: Receive = {
    case Initialize(_) => sender() ! AlreadyInitialized

    case GetState(x, y) => sender() ! states.get(x, y)
    case GetHeight(x, y) => sender() ! heights.get(x, y)

    case SetStateAndHeight(x, y, state, height) =>
      states.set(x, y)(state)
      heights.set(x, y)(height)

    case GetDoorPosition => sender() ! doorPosition
    case GetAbsoluteHeightMap => sender() ! absoluteHeightMap

    case BlockTileTowardsDestination(x, y) =>
      sender() ! Some(Vector3(x, y, heights.get(x, y).getOrElse(0)))
  }
}

object MapCoordinator {
  case class GetState(x: Int, y: Int)
  case class GetHeight(x: Int, y: Int)
  case class SetStateAndHeight(x: Int, y:Int, state: MapUnit, height: Int)
  case object GetDoorPosition
  case object GetAbsoluteHeightMap
  case class BlockTileTowardsDestination(x: Int, y: Int)

  case object InitializedFallback

  def props(modelRepository: RoomModelRepository) = Props(classOf[MapCoordinator], modelRepository)
  def props(): Props = props(RoomModelRepository)
}