package de.lightwave.rooms.engine.mapping

import akka.actor.{ActorLogging, Props}
import de.lightwave.rooms.engine.EngineComponent
import de.lightwave.rooms.engine.EngineComponent.{AlreadyInitialized, Initialize, Initialized}
import de.lightwave.rooms.engine.mapping.MapCoordinator._
import de.lightwave.rooms.engine.mapping.RoomMap.StaticMap
import de.lightwave.rooms.engine.mapping.pathfinding.{AStarPathfinder, Pathfinder}
import de.lightwave.rooms.model.{RoomModel, RoomModels}
import de.lightwave.rooms.repository.RoomModelRepository

/**
  * Represents source of truth regarding all map states
  * and path finding of a room.
  */
class MapCoordinator(modelRepository: RoomModelRepository, pathfinder: Pathfinder) extends EngineComponent with ActorLogging {
  // Define maps that are responsible for storing heights
  // and states of all tiles
  var heights = new RoomMap[Double](0, 0)
  implicit var states  = new RoomMap[MapUnit](0, 0)

  // Let's call it pseudo-cache
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

  /**
    * Block tile so that players can't walk on it
    * @return true if tile was blocked otherwise false
    */
  def blockTile(x: Int, y: Int): Boolean = states.get(x, y) match {
    case Some(state) => state match {
      case _:MapUnit.Tile if state != MapUnit.Tile.Blocked =>
        states.set(x, y)(MapUnit.Tile.Blocked)
        true
      case _ => false
    }
    case None => false
  }

  /**
    * Clear tile so that it's walkable again
    * @return true if tile was cleared otherwise false
    */
  def clearTile(x: Int, y: Int): Boolean = states.get(x, y) match {
    case Some(state) => state match {
      case MapUnit.Tile.Blocked =>
        states.set(x, y)(MapUnit.Tile.Clear)
        true
      case _ => false
    }
    case None => false
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

    case ClearTile(x, y) => clearTile(x, y)
    case BlockTile(x, y) => blockTile(x, y)
    case BlockTileTowardsDestination(from, to) => sender() ! (pathfinder.findNextStep(from, to) match {
      case Some(nextStep) =>
        blockTile(nextStep.x, nextStep.y)
        Some(Vector3(nextStep.x, nextStep.y, heights.get(nextStep.x, nextStep.y).getOrElse(0)))
      case None => None
    })
  }
}

object MapCoordinator {
  case class GetState(x: Int, y: Int)
  case class GetHeight(x: Int, y: Int)
  case class SetStateAndHeight(x: Int, y:Int, state: MapUnit, height: Int)
  case object GetDoorPosition
  case object GetAbsoluteHeightMap
  case class BlockTile(x: Int, y: Int)
  case class ClearTile(x: Int, y: Int)
  // Find a path to destination and block next step
  case class BlockTileTowardsDestination(from: Vector2, to: Vector2)

  case object InitializedFallback

  def props(modelRepository: RoomModelRepository, pathfinder: Pathfinder) = Props(classOf[MapCoordinator], modelRepository, pathfinder)
  def props(): Props = props(RoomModelRepository, AStarPathfinder)
}