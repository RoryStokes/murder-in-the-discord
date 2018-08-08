package es.rorystok.mitd.discord

import java.util.concurrent.TimeUnit

import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.hooks.EventListener
import net.dv8tion.jda.core.requests.restaction.{AuditableRestAction, ChannelAction}
import net.dv8tion.jda.core.{AccountType, JDABuilder, Permission}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.implicitConversions

class DiscordService {
  val builder = new JDABuilder(AccountType.BOT)

  def withListener(listener: EventListener) = {
    builder.addEventListener(listener)
    this
  }


  def start(token: String) = {
    builder.setToken(token).buildBlocking()
    this
  }

}

object DiscordService {
  def apply(token: String, listeners: Seq[EventListener]) = {
    listeners.foldLeft(new DiscordService) { case (service, listener) =>
        service.withListener(listener)
    }.start(token)
  }

  def execute[T](action: AuditableRestAction[T]): Future[T] = Future {
    action.complete()
  }

  def executeAfter[T](action: AuditableRestAction[T], delayMs: Long): Future[T] = Future {
    action.completeAfter(delayMs, TimeUnit.MILLISECONDS)
  }

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