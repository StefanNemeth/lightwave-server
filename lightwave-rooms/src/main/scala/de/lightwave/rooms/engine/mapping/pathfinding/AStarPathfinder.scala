package de.lightwave.rooms.engine.mapping.pathfinding
import de.lightwave.rooms.engine.mapping.{MapUnit, RoomMap, Vector2}

/**
  * "Simple" A-Star algorithm implementation
  */
object AStarPathfinder extends Pathfinder {
  // The lower the f, the better
  private object MinOrder extends Ordering[PathNode] {
    def compare(x: PathNode, y: PathNode): Int = y.f compare x.f
  }

  // As the tiles are adjacent, distance is always 1 (cost for getting on a chair, etc.?)
  private def calculateCost(from: Vector2, to: Vector2): Int = 1

  // Use manhattan distance in order to avoid unnecessary diagonal
  // moves
  private def estimateCost(from: Vector2, to: Vector2): Int = math.abs(from.x - to.x) + math.abs(from.y - to.y)

  private def findPath(startNode: PathNode, destination: Vector2)(implicit states: RoomMap[MapUnit]): Option[PathNode] = {
    implicit val openList = collection.mutable.PriorityQueue.empty(MinOrder)
    implicit val closedList = collection.mutable.Set.empty[Vector2]

    openList.enqueue(startNode)

    while (openList.nonEmpty) {
      val node = openList.dequeue()

      if (node.pos == destination) {
        return Some(node)
      }

      closedList.add(node.pos)
      expandNode(node, destination)
    }

    None
  }

  private def expandNode(node: PathNode, destination: Vector2)(implicit openList: collection.mutable.PriorityQueue[PathNode], closedList: collection.mutable.Set[Vector2], states: RoomMap[MapUnit]): Unit =
    for (neighbourPos <- Pathfinder.getPotentialNeighbours(node.pos)) if (states.get(neighbourPos.x, neighbourPos.y).contains(MapUnit.Tile.Clear) && !closedList.contains(neighbourPos)) {
      val costs = node.g + calculateCost(node.pos, neighbourPos)
      val neighbourNode = PathNode(neighbourPos, Some(node), costs + estimateCost(neighbourPos, destination), costs)

      // Improve this part?
      def overridePath: Boolean = {
        while (openList.nonEmpty) {
          val testNode = openList.dequeue()
          if (testNode.pos == neighbourNode.pos) {
            if (testNode.g <= costs) {
              openList.enqueue(testNode)
              return false
            }
            return true
          }
          openList.enqueue(testNode)
          return true
        }
        true
      }

      if (overridePath) {
        openList.enqueue(neighbourNode)
      }
    }

  override def findNextStep(currentPosition: Vector2, destination: Vector2)(implicit states: RoomMap[MapUnit]): Option[Vector2] = {
    findPath(PathNode(currentPosition), destination).orElse(SimplePathfinder.findNextStep(currentPosition, destination)) match {
      case Some(lastStep: PathNode) =>
        var temp: PathNode = lastStep
        var firstStep: Option[Vector2] = None
        while (firstStep.isEmpty && temp.previousNode.isDefined) {
          if (temp.previousNode.get.previousNode.isEmpty) {
            firstStep = Some(temp.pos)
          }
          temp = temp.previousNode.get
        }
        firstStep
      case Some(alternative: Vector2) => Some(alternative)
      case None => None
    }
  }
}
