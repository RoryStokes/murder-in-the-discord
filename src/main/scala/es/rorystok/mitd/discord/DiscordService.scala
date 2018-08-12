package es.rorystok.mitd.discord

import java.util

import es.rorystok.mitd.model.{PlayerRef, RoomRef}
import net.dv8tion.jda.core.entities.{Channel, Guild, Member, Role}
import net.dv8tion.jda.core.hooks.EventListener
import net.dv8tion.jda.core.requests.restaction.{AuditableRestAction, ChannelAction}
import net.dv8tion.jda.core.{AccountType, JDA, JDABuilder, Permission}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

class DiscordService(token: String, listeners: Seq[EventListener]) {
  val jda: JDA = listeners.foldLeft(new JDABuilder(AccountType.BOT)) { case (builder, listener) =>
    builder.addEventListener(listener)
  }.setToken(token).buildBlocking()

}

object DiscordService {

  def roomRef(channel: Channel) = RoomRef(channel.getName, channel.getId)
  def playerRef(member: Member) = PlayerRef(member.getEffectiveName, member.getUser.getId)

  def getMembersInRoom(channelName: String, guild: Guild): Option[(RoomRef, Seq[Member])] = {
    guild.getVoiceChannelsByName(channelName, false).asScala.headOption
      .map(channel => (roomRef(channel), channel.getMembers.asScala))
  }

  def setVoiceChannels(channelNames: Seq[String], guild: Guild): Future[Unit] = {
    val targetNames = channelNames.toSet
    val currentChannels = guild.getVoiceChannels.asScala

    for {
      _ <- Future.sequence(currentChannels.filterNot(targetNames contains _.getName)
        .map(ch => sequenceAction(ch.delete())))
      _ <- Future.sequence(
        targetNames.diff(currentChannels.map(_.getName).toSet) map { name =>
          val channel = guild.getController.createVoiceChannel(name)
          sequenceAction(channel)
        }
      )
      _ <- Future.sequence(currentChannels.filter(targetNames contains _.getName) map { channel =>
        channel.getPermissionOverrides.asScala.foreach(_.delete.queue)
        sequenceAction(channel.putPermissionOverride(guild.getPublicRole).setAllow(viewPermissions))
      })

    } yield ()
  }

  def setChannelAccessForUser(player: PlayerRef, room: RoomRef, allowed: Boolean, guild: Guild): Unit = {
    val member = guild.getMemberById(player.userId)
    val channel = guild.getVoiceChannelById(room.channelId)
    val permissionOverride = channel.putPermissionOverride(member)
    if (allowed) permissionOverride.setAllow(DiscordService.viewPermissions)
    else permissionOverride.setDeny(DiscordService.viewPermissions)
    permissionOverride.queue()
  }

  def sequenceAction[T](action: AuditableRestAction[T]): Future[T] = Future {
    action.complete()
  }

  val viewPermissions: util.Collection[Permission] = Seq(Permission.VIEW_CHANNEL).asJavaCollection
  val noPermissions: util.Collection[Permission] = Nil.asJavaCollection

  private def grantPermissions(channel: ChannelAction, role: Role,  permissions: Seq[Permission]) = {
    channel.addPermissionOverride(
      role, permissions.asJavaCollection, Seq[Permission]().asJavaCollection
    )
  }

  private def restrictPermissions(channel: ChannelAction, role: Role,  permissions: Seq[Permission]) = {
    channel.addPermissionOverride(
      role, Seq[Permission]().asJavaCollection, permissions.asJavaCollection
    )
  }
}