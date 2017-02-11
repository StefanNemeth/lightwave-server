package de.lightwave.players

import akka.actor.{ActorRef, Props}
import akka.cluster.Cluster
import akka.cluster.ddata.Replicator._
import akka.cluster.ddata.{DistributedData, LWWMap, LWWMapKey}
import de.lightwave.players.PlayerService._
import de.lightwave.players.helper.PasswordHash
import de.lightwave.players.model.{Player, SecurePlayer}
import de.lightwave.players.model.Players.PlayerId
import de.lightwave.players.repository.PlayerRepository
import de.lightwave.services.Service

import scala.concurrent.Future
import scala.util.Random

/**
  * Service that provides player information and
  * creates them
  */
class PlayerService(playerRepository: PlayerRepository) extends Service {
  import akka.pattern._
  import context.dispatcher

  implicit val cluster = Cluster(context.system)

  val replicator = DistributedData(context.system).replicator
  val DataKey = LWWMapKey[Player]("player")

  def getPlayerById(playerId: PlayerId): Future[Option[Player]] = {
    val playerFuture = playerRepository.getById(playerId).map {
      case Some(player) => Some(Player.from(player))
      case None => None
    }

    playerFuture foreach handleFetchResponse
    playerFuture
  }

  def getPlayerByNickname(nickname: String): Future[Option[Player]] = {
    val playerFuture = playerRepository.getByNickname(nickname).map {
      case Some(player) => Some(Player.from(player))
      case None => None
    }

    playerFuture foreach handleFetchResponse
    playerFuture
  }

  /**
    * Look for player with given nickname and password.
    * Future results in Some[Player] if player is found, otherwise None.
    */
  def authenticatePlayer(nickname: String, password: String): Future[Option[Player]] = {
    val playerFuture = playerRepository.getByNickname(nickname) map {
      case Some(player) if player.password.sameElements(PasswordHash.hash(password.toCharArray, player.salt)) => Some(Player.from(player))
      case _ => None
    }

    playerFuture foreach handleFetchResponse
    playerFuture
  }

  /**
    * Caches player after successful fetch
    */
  def handleFetchResponse: PartialFunction[Option[Player], Unit] = {
    case Some(player) => self ! PutInCache(player.id.getOrElse(Random.nextInt()), player)
    case None => // Ignore if not existent
  }

  override def receive = {
    case GetPlayer(id) => replicator ! Get(DataKey, ReadLocal, Some(RequestPlayer(id, sender())))
    case GetPlayerByNickname(nickname) => getPlayerByNickname(nickname) pipeTo sender()
    case AuthenticatePlayer(nickname, password) => authenticatePlayer(nickname, password) pipeTo sender()

    case PutInCache(id, player) => replicator ! Update(DataKey, LWWMap.empty[Player], WriteLocal)(_ + (id.toString -> player))

    case g @ GetSuccess(_, Some(RequestPlayer(playerId, replyTo))) => g.dataValue match {
      case data: LWWMap[_] => data.get(playerId.toString) match {
        case Some(player) => replyTo ! Some(player)
        case None => getPlayerById(playerId) pipeTo replyTo
      }
    }

    case NotFound(_, Some(RequestPlayer(playerId, replyTo))) => getPlayerById(playerId) pipeTo replyTo
  }
}

object PlayerService {
  case class GetPlayer(id: PlayerId)
  case class GetPlayerByNickname(nickname: String)
  case class RequestPlayer(id: PlayerId, recipient: ActorRef)
  case class PutInCache(id: PlayerId, player: Player)

  /**
    * Looks for player with given nickname and password.
    * Sends back Some[Player] if player is found, otherwise None.
    */
  case class AuthenticatePlayer(nickname: String, password: String)

  // Use Postgres repository by default
  def props(): Props = props(PlayerRepository)

  def props(playerRepository: PlayerRepository) = Props(classOf[PlayerService], playerRepository)
}
