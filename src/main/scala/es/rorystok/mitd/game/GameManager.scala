package es.rorystok.mitd.game

import java.util.concurrent.TimeUnit

import es.rorystok.mitd.discord.DiscordService
import es.rorystok.mitd.model._
import es.rorystok.mitd.state.GameAction.{PlayerMissing, PlayerMoved, PlayerMoving}
import es.rorystok.mitd.state.GameCircuit
import es.rorystok.mitd.Util._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

class GameManager(circuit: GameCircuit) {
  val roomManager = new RoomManager(circuit)
  val playerManager = new PlayerManager(circuit)
  //private val guild = circuit.zoom(_.discord.guild)

  //withDiscord(discord => Await.result(roomManager.init(discord), FiniteDuration(30, TimeUnit.SECONDS)))

  //playerManager.players.foreach(subscribePlayerLocation)
//  RoomManager.rooms.foreach(subscribeRoomAccess)
//
//
//  def withDiscord[A](fn: DiscordService => A): Option[A] = {
//    guild().map(new DiscordService(_)).map(fn)
//  }
//
//  def setRoomAccess(room: Room, userIds: Seq[UserId], players: Seq[Player]): Option[Unit] = {
//    println("SETTING ROOM ACCESS")
//    println(userIds)
//    println(players)
//    val toRevoke = userIds.filterNot(uid => players.exists(_.userId == uid))
//    val toAllow = players.map(_.userId).filterNot(uid => userIds.contains(uid))
//    withDiscord { discordService =>
//      toRevoke.foreach(discordService.setChannelAccess(room.name, _, allowed = false))
//      toAllow.foreach(discordService.setChannelAccess(room.name, _, allowed = true))
//    }
//  }
//
//  private def subscribeRoomAccess(room: Room): Unit = {
//    val desiredAccessLens = circuit.zoom(_.players.values.filter(player => playerManager.playerCanSee(player, room)))
//    val currentAccessLens = circuit.zoom(_.discord.channels.get(room.name).map(_.hasAccess))
//    subscribeZipped(circuit, desiredAccessLens, currentAccessLens) {
//      case (desiredAccess, currentAccess) => setRoomAccess(room, currentAccess.getOrElse(Nil), desiredAccess.toSeq)
//    }
//  }
//
//
//  private def subscribePlayerLocation(player: Player): Unit = {
//    val userId = player.userId
//    println(s"subscribed to $userId movements")
//    val locationLens = circuit.zoom(_.players.get(userId).map(_.location))
//    val memberLens = circuit.zoom(_.discord.members.get(userId))
//
//    subscribeZipped(circuit, locationLens, memberLens) {
//      case (Some(location), Some(member)) =>
//        ensureUserDeafened(userId, target = location.isInstanceOf[Moving], current = member.deafened)
//        location match {
//          case Missing(room) =>
//            ensureUserInRoom(userId, room, member.channel)
//            if(member.channel.nonEmpty) circuit.dispatch(PlayerMoved(userId, room))
//          case Moving(_, room) =>
//            ensureUserInRoom(userId, room, member.channel)
//          case InRoom(room) =>
//            member.channel match {
//              case Some(to) if to != room => circuit.dispatch(PlayerMoving(userId, room, to))
//              case None => circuit.dispatch(PlayerMissing(userId, room))
//              case _ => ()
//            }
//        }
//      case bad =>
//        println(bad)
//    }
//  }
//
//
//
//  def ensureUserInRoom(userId: UserId, target: String, current: Option[String]): Unit = {
//    if(current.contains(target)) withDiscord(_.movePlayerToChannel(userId, target))
//  }
//  def ensureUserDeafened(userId: UserId, target: Boolean, current: Boolean): Unit = {
//    if(current != target) withDiscord(_.setPlayerDeafened(userId, target))
//  }

}
