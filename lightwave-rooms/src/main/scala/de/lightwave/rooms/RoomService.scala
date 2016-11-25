package de.lightwave.rooms

import akka.actor.{ActorRefFactory, Props}
import akka.cluster.routing.{ClusterRouterGroup, ClusterRouterGroupSettings}
import akka.routing.{FromConfig, RoundRobinGroup}
import de.lightwave.services.{Service, ServiceHelper}

class RoomService extends Service {
  def receive = { case _ => }
}

object RoomService extends ServiceHelper {
  val ServiceName = "roomService"
  val ClusterRole = "rooms"

  def props() = Props(classOf[RoomService])

  // Default room service router
  override def createRouter(factory: ActorRefFactory) = factory.actorOf(
    ClusterRouterGroup(
      RoundRobinGroup(Nil),
      ClusterRouterGroupSettings(
        totalInstances = 100,
        routeesPaths = List(s"/user/${RoomService.ServiceName}"),
        allowLocalRoutees = true,
        useRole = Some(RoomService.ClusterRole)
      )
    ).props()
  )

  override def createRouter(factory: ActorRefFactory, name: String) = factory.actorOf(FromConfig.props(), name)
}
