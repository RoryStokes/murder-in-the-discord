package es.rorystok.mitd.game

import java.util.concurrent.TimeUnit

import es.rorystok.mitd.discord.DiscordService
import es.rorystok.mitd.game.RoomManager.rooms
import es.rorystok.mitd.model.{Player, Room, UserId}
import es.rorystok.mitd.state.GameAction.CreateRooms
import es.rorystok.mitd.state.GameCircuit
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild

import scala.collection.JavaConverters._
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.FiniteDuration
import scala.language.implicitConversions

class GameManager(circuit: GameCircuit) {
  val roomManager = new RoomManager(circuit)
  val playerManager = new PlayerManager(circuit)

  withDiscord(discord => Await.result(roomManager.init(discord), FiniteDuration(30, TimeUnit.SECONDS)))
  rooms foreach { room =>
    println(room)
    val userLens = circuit.zoom(_.guild.toList.flatMap(g => allowedUsersForChannel(g, room)))
    val playerLens = circuit.zoom(_.rooms.filter(_.id == room.id).flatMap(playerManager.playersWhoSeeRoom))
    circuit.subscribe(userLens zip playerLens) { value =>
      val (userIds, players) = value()
      setRoomAccess(room, userIds, players)
    }
    setRoomAccess(room, userLens(), playerLens())
  }



  def allowedUsersForChannel(guild: Guild, room: Room): Seq[UserId] = for {
    channel <- guild.getVoiceChannelsByName(room.name, false).asScala
    permission <- channel.getMemberPermissionOverrides.asScala if permission.getAllowed.asScala.contains(Permission.VIEW_CHANNEL)
  } yield permission.getMember.getUser.getId

  def withDiscord[A](fn: DiscordService => A): Option[A] = {
    val guild = circuit.zoom(_.guild)
    guild().map(new DiscordService(_)).map(fn)
  }

  def setRoomAccess(room: Room, userIds: Seq[UserId], players: Seq[Player]): Option[Unit] = {
    val toRevoke = userIds.filterNot(uid => players.exists(_.userId == uid))
    val toAllow = players.map(_.userId).filterNot(uid => userIds.contains(uid))
    withDiscord { discordService =>
      toRevoke.foreach(discordService.setChannelAccess(room.name, _, allowed = false))
      toAllow.foreach(discordService.setChannelAccess(room.name, _, allowed = true))
    }
  }

//  def onRoomEnter(e: GenericGuildVoiceEvent): Unit = {
//    val controller = e.getGuild.getController
//    Future {
//      controller.setDeafen(e.getMember, true).complete()
//      controller.removeRolesFromMember(e.getMember, e.getMember.getRoles.asScala.filter(_.getName.startsWith("$r_")).asJavaCollection).complete()
//      controller.addSingleRoleToMember(e.getMember, e.getGuild.getRolesByName("$moving", false).get(0)).complete()
//    }
//  }
//
//  def onRoleAdded(e: GuildMemberRoleAddEvent): Unit = {
//    val controller = e.getGuild.getController
//    println(e.getRoles.asScala.map(_.getName))
//    val room = e.getMember.getVoiceState.getChannel
//    if(e.getRoles.asScala.exists(_.getName == "$moving")) {
//      Future {
//        Thread.sleep(2000)
//        controller.setDeafen(e.getMember, false).complete()
//        controller.removeSingleRoleFromMember(e.getMember, e.getGuild.getRolesByName("$moving", false).get(0)).complete()
//        controller.addSingleRoleToMember(e.getMember, e.getGuild.getRolesByName(Util.roleForRoom(room.getName), false).get(0)).complete()
//        controller.moveVoiceMember(e.getMember, room)
//      }
//    }
//  }
}
