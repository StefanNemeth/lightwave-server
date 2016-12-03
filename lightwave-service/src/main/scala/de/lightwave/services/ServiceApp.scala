package de.lightwave.services

import akka.actor.ActorSystem
import akka.cluster.Cluster
import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.Logger

import scala.collection.JavaConversions._

/**
  * Services that can be run independently should extend this trait
  * for the sake of consistency.
  */
trait ServiceApp {
  def main(args: Array[String]): Unit = {
    val servicePort = args match {
      case Array()     => "0"
      case Array(port) => port
      case _           => throw new IllegalArgumentException(s"only ports. Args [ $args ] are invalid")
    }

    val properties = Map(
      "akka.remote.netty.tcp.port" -> servicePort
    )

    val config = ConfigFactory.parseMap(properties).withFallback(ConfigFactory.load())
    val system = ActorSystem(ServiceApp.SystemName, config)

    ServiceApp.Log.info(s"Starting [${config.getStringList("akka.cluster.roles").mkString(",")}] service app..")
    ServiceApp.Log.info("Waiting for cluster member to be 'Up'..")

    val cluster = Cluster(system)

    cluster registerOnMemberUp {
      ServiceApp.Log.info(s"Service node up on '${cluster.selfAddress}'!")
      onStart(config, system)
    }
  }

  /**
    * To be defined by the service implementation.
    * It's called when the cluster is ready to go.
    */
  def onStart(config: Config, system: ActorSystem): Unit
}

object ServiceApp {
  val Log = Logger[ServiceApp]

  // Global actor system name
  val SystemName = "lightwave"
}