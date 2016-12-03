package de.lightwave.services

import akka.actor.{Actor, ActorRef, ActorRefFactory}
import akka.cluster.routing.{ClusterRouterGroup, ClusterRouterGroupSettings}
import akka.routing.{FromConfig, RoundRobinGroup}

/**
  * Lightwave makes use of a microservice architecture. Services represent the main components of the server and the project.
  *
  * A service can provide with:
  * - Data from databases and remote APIs (application cache layer)
  * - (Persistent) workers
  * - Execution contexts
  *
  * They should always be scalable, extendable, reliable and fault-tolerant.
  */
trait Service extends Actor

object ServiceGroups {
  /**
    * Create a configurable group that routes to existent instances of a
    * specific service.
    *
    * @param factory The context to be used
    * @param name Configuration name
    * @return The created group
    */
  def createGroup(factory: ActorRefFactory, name: String): ActorRef = factory.actorOf(
    FromConfig.props(), name = name
  )

  /**
    * Create default round robin group that routes to existent instances of
    * a specific service.
    *
    * @param factory The context to be used
    * @return The created group
    */
  def createGroup(factory: ActorRefFactory, serviceName: String, role: Option[String]): ActorRef = factory.actorOf(
    ClusterRouterGroup(
      RoundRobinGroup(Nil), ClusterRouterGroupSettings(
        totalInstances = 100,
        routeesPaths = List(s"/user/$serviceName"),
        allowLocalRoutees = true,
        useRole = role
      )
    ).props(), name = s"${serviceName}Group"
  )
}