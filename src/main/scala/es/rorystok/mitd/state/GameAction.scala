package es.rorystok.mitd.state

import diode.Action
import es.rorystok.mitd.model._
import net.dv8tion.jda.core.entities.Guild

sealed trait GameAction extends Action

object GameAction {
  case class CreatePlayers(players: Seq[Player]) extends GameAction

  sealed trait PlayerUpdate extends GameAction {
    def player: PlayerRef
  }
  case class PlayerMoving(player: PlayerRef, to: RoomRef) extends PlayerUpdate
  case class PlayerMoved(player: PlayerRef, to: RoomRef) extends PlayerUpdate
  case class PlayerMissing(player: PlayerRef) extends PlayerUpdate

  case class InitialiseRoom(ref: RoomRef) extends GameAction

  case class ChannelAccessChanged(userId: UserId, channelName: String, allowed: Boolean) extends GameAction

  case class DiscordResult[T](result: T) extends GameAction
  case class DiscordUpdate(guild: Guild) extends GameAction

}