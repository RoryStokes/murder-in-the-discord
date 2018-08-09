package es.rorystok.mitd.state

import diode.Action
import es.rorystok.mitd.model.{Player, Room, RoomId, UserId}
import net.dv8tion.jda.core.entities.Guild

sealed trait GameAction extends Action

object GameAction {

  case class CreateRooms(rooms: Seq[Room]) extends GameAction

  case class CreatePlayers(players: Seq[Player]) extends GameAction
  case class PlayerMoved(userId: UserId, roomId: RoomId, guild: Guild) extends GameAction
  case class PlayerMovingStart(userId: UserId, guild: Guild) extends GameAction
  case class PlayerMovingEnd(userId: UserId, guild: Guild) extends GameAction

  case class DiscordResult[T](result: T) extends GameAction
  case class DiscordUpdate(guild: Guild) extends GameAction

}