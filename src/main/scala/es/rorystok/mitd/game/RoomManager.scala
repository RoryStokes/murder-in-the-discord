package es.rorystok.mitd.game

import es.rorystok.mitd.discord.DiscordService
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.{Guild, IPermissionHolder}
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.guild.member.GuildMemberRoleAddEvent
import net.dv8tion.jda.core.events.guild.voice.{GenericGuildVoiceEvent, GuildVoiceJoinEvent, GuildVoiceMoveEvent}
import net.dv8tion.jda.core.hooks.EventListener
import es.rorystok.mitd.Util
import es.rorystok.mitd.model.Room

import collection.JavaConverters._
import scala.concurrent.Future
import scala.language.implicitConversions
import scala.concurrent.ExecutionContext.Implicits.global

class RoomManager(rooms: Seq[Room], passages: Seq[(Room, Room)]) extends EventListener {
  val forward: Map[String, Seq[Room]] = passages.groupBy(_._1.id).mapValues(list => list.map(_._2))
  val backward: Map[String, Seq[Room]] = passages.groupBy(_._2.id).mapValues(list => list.map(_._1))

  val passageMap: Map[String, Seq[Room]] = rooms.map { room =>
    (room.id, forward.getOrElse(room.id, Nil) ++ backward.getOrElse(room.id, Nil))
  }.toMap

  override def onEvent(event: Event): Unit = event match {
    case e: GuildVoiceMoveEvent => onRoomEnter(e)
    case e: GuildVoiceJoinEvent => onRoomEnter(e)
    case e: GuildMemberRoleAddEvent => onRoleAdded(e)
    case _ => ()
  }


  def onRoomEnter(e: GenericGuildVoiceEvent): Unit = {
    val controller = e.getGuild.getController
    Future {
      controller.setDeafen(e.getMember, true).complete()
      controller.removeRolesFromMember(e.getMember, e.getMember.getRoles.asScala.filter(_.getName.startsWith("$r_")).asJavaCollection).complete()
      controller.addSingleRoleToMember(e.getMember, e.getGuild.getRolesByName("$moving", false).get(0)).complete()
    }
  }

  def onRoleAdded(e: GuildMemberRoleAddEvent): Unit = {
    val controller = e.getGuild.getController
    println(e.getRoles.asScala.map(_.getName))
    val room = e.getMember.getVoiceState.getChannel
    if(e.getRoles.asScala.exists(_.getName == "$moving")) {
      Future {
        Thread.sleep(2000)
        controller.setDeafen(e.getMember, false).complete()
        controller.removeSingleRoleFromMember(e.getMember, e.getGuild.getRolesByName("$moving", false).get(0)).complete()
        controller.addSingleRoleToMember(e.getMember, e.getGuild.getRolesByName(Util.roleForRoom(room.getName), false).get(0)).complete()
        controller.moveVoiceMember(e.getMember, room)
      }
    }
  }

  def init(guild: Guild): Unit = {
    guild.getVoiceChannels.asScala.foreach(_.delete().queue())
    rooms foreach { room =>
      val channel = guild.getController
        .createVoiceChannel(room.name)
      if(room.name != "Main Hall") {
        DiscordService.restrictPermissions(channel, guild.getPublicRole, Seq(Permission.VIEW_CHANNEL))
      }
      passageMap.getOrElse(room.id, Nil)
        .flatMap(connectedRoom => guild.getRolesByName(Util.roleForRoom(connectedRoom), false).asScala)
        .foreach(role =>
          DiscordService.grantPermissions(channel, role, Seq(Permission.VIEW_CHANNEL))
        )
      channel.queue()
    }
  }
}