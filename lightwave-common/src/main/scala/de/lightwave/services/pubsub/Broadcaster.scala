package de.lightwave.services.pubsub

import akka.actor.{Actor, ActorRef, Props}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, Unsubscribe}

/**
  * Simple actor that makes it easy to subscribe and publish to
  * a specific topic without having to share it directly
  */
class Broadcaster(topic: String, mediator: ActorRef) extends Actor {
  override def receive: Receive = {
    case Broadcaster.Publish(msg) => mediator ! Publish(topic, msg)
    case Broadcaster.Subscribe(actor) => mediator ! Subscribe(topic, actor)
    case Broadcaster.Unsubscribe(actor) => mediator ! Unsubscribe(topic, actor)
  }
}

object Broadcaster {
  case class Publish(msg: Any)
  case class Subscribe(actor: ActorRef)
  case class Unsubscribe(actor: ActorRef)

  def props(topic: String)(implicit mediator: ActorRef) = Props(classOf[Broadcaster], topic, mediator)
}