package es.rorystok.mitd.discord


import diode.{Action, Effect}
import es.rorystok.mitd.model._
import es.rorystok.mitd.state.GameAction.InitialiseRoom
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.requests.restaction.ChannelAction

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions


sealed trait DiscordAction extends Action {
  def execute(guild: Guild): Option[Effect]
}

object DiscordAction {
  case class SetPlayerAccess(player: Player, rooms: Seq[RoomRef]) extends DiscordAction {
    override def execute(guild: Guild): Option[Effect] = {
      val toAllow = rooms.filterNot(player.canSee contains _)
      val toRevoke = player.canSee.filterNot(rooms contains _)

      toAllow.foreach(room => DiscordService.setChannelAccessForUser(player.ref, room, allowed = true, guild))
      toRevoke.foreach(room => DiscordService.setChannelAccessForUser(player.ref, room, allowed = false, guild))

      None
    }
  }

  case class SetPlayerDeafened(player: PlayerRef, deafen: Boolean = true) extends DiscordAction {
    override def execute(guild: Guild): Option[Effect] = {
      val member = guild.getMemberById(player.userId)
      guild.getController.setDeafen(member, deafen).queue()
      None
    }
  }

  case class MovePlayerToChannel(player: PlayerRef, room: RoomRef) extends DiscordAction {
    override def execute(guild: Guild): Option[Effect] = {
      val member = guild.getMemberById(player.userId)
      val channel = guild.getVoiceChannelById(room.channelId)
      guild.getController.moveVoiceMember(member, channel).queue()
      None
    }
  }

  case class CreateOrFetchVoiceChannel(roomName: String, visible: Boolean) extends DiscordAction {
    override def execute(guild: Guild): Option[Effect] = Some {
      guild.getVoiceChannelsByName(roomName, false).asScala.headOption match {
        case Some(channel) => Effect.action(InitialiseRoom(RoomRef(roomName, channel.getId)))
        case None => Effect {
          val channel = guild.getController.createVoiceChannel(roomName)
          val action: ChannelAction = if (visible) channel else {
            channel.addPermissionOverride(guild.getPublicRole, DiscordService.noPermissions, DiscordService.viewPermissions)
          }
          DiscordService.sequenceAction(action).map(channel => InitialiseRoom(RoomRef(roomName, channel.getId)))
        }
      }
    }
  }
}