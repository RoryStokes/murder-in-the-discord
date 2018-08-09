package es.rorystok.mitd.discord

import java.util
import java.util.concurrent.TimeUnit

import es.rorystok.mitd.model.UserId
import net.dv8tion.jda.core.entities.{Guild, Member, Role}
import net.dv8tion.jda.core.hooks.EventListener
import net.dv8tion.jda.core.managers.GuildController
import net.dv8tion.jda.core.requests.restaction.{AuditableRestAction, ChannelAction}
import net.dv8tion.jda.core.{AccountType, JDABuilder, Permission}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

class DiscordService(guild: Guild) {
  import DiscordService._

  val controller: GuildController = guild.getController

  def getMembersInRoom(channelName: String): Seq[Member] = {
    guild.getVoiceChannelsByName(channelName, false).asScala
      .flatMap(_.getMembers.asScala)
  }

  def createOrFetchUserRole(userId: UserId): Future[Role] = {
    val roleName = s"$$p_$userId"
    println(roleName)
    val maybeRole = guild.getRolesByName(roleName, false).asScala.headOption

    (maybeRole match {
      case Some(existingRole) => Future.successful(existingRole)
      case None => execute(controller.createRole().setName(roleName))
    }) map { role =>
      println("Assigning")
      val member = guild.getMemberById(userId)
      if(!member.getRoles.contains(role)) {
        controller.addSingleRoleToMember(member, role).queue()
      }
      role
    }
  }

  def setChannelAccess(channelName: String, userId: UserId, allowed: Boolean) = {
    val member = guild.getMemberById(userId)
    println(s"cn $channelName, ui $userId, all $allowed")
    guild.getVoiceChannelsByName(channelName, false).asScala foreach { channel =>
      println("found vc")
      val permissionOverride = channel.createPermissionOverride(member)
      if(allowed) permissionOverride.setAllow(viewPermissions)
      else permissionOverride.setDeny(viewPermissions)
      permissionOverride.queue()
    }
  }

  def setPlayerDeafened(userId: UserId, deafen: Boolean = true): Future[Void] = {
    val member = guild.getMemberById(userId)
    execute(controller.setDeafen(member, deafen))
  }

  def setVoiceChannels(channelNames: Seq[String], hidden: Boolean = false): Future[Unit] = {
    val targetNames = channelNames.toSet
    val currentChannels = guild.getVoiceChannels.asScala

    for {
      _ <- Future.sequence(currentChannels.filterNot(targetNames contains _.getName).map(ch => execute(ch.delete())))
      _ <- Future.sequence(
        targetNames.diff(currentChannels.map(_.getName).toSet) map { name =>
          val channel = controller.createVoiceChannel(name)
          if (hidden) {
            channel.addPermissionOverride(guild.getPublicRole, noPermissions, viewPermissions)
          }
          execute(channel)
        }
      )
    } yield ()
  }
}

object DiscordService {
  def apply(guild: Guild): DiscordService = new DiscordService(guild)

  def start(token: String, listeners: Seq[EventListener]) = {
    listeners.foldLeft(new JDABuilder(AccountType.BOT)) { case (builder, listener) =>
      builder.addEventListener(listener)
    }.setToken(token).buildBlocking()
  }

  def execute[T](action: AuditableRestAction[T]): Future[T] = Future {
    action.complete()
  }

  def executeAfter[T](action: AuditableRestAction[T], delayMs: Long): Future[T] = Future {
    action.completeAfter(delayMs, TimeUnit.MILLISECONDS)
  }

  val viewPermissions: util.Collection[Permission] = Seq(Permission.VIEW_CHANNEL).asJavaCollection
  val noPermissions: util.Collection[Permission] = Nil.asJavaCollection

  def grantPermissions(channel: ChannelAction, role: Role,  permissions: Seq[Permission]) = {
    channel.addPermissionOverride(
      role, permissions.asJavaCollection, Seq[Permission]().asJavaCollection
    )
  }

  def restrictPermissions(channel: ChannelAction, role: Role,  permissions: Seq[Permission]) = {
    channel.addPermissionOverride(
      role, Seq[Permission]().asJavaCollection, permissions.asJavaCollection
    )
  }
}