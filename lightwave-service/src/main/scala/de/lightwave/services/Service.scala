package de.lightwave.services

import akka.actor.{Actor, ActorRef, ActorRefFactory}

trait Service extends Actor {
}

trait ServiceHelper {
  // Create router using custom configuration
  def createRouter(factory: ActorRefFactory, name: String): ActorRef

  // Create default router
  def createRouter(factory: ActorRefFactory): ActorRef
}