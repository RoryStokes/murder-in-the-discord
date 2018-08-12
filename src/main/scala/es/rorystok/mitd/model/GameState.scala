package es.rorystok.mitd.model

import net.dv8tion.jda.core.entities.Guild

final case class GameState(
  players: Map[UserId, Player],
  rooms: Map[ChannelId, Room],
  guild: Option[Guild]
)

object GameState {
  val initialState = GameState(Map.empty, Map.empty, None)
}
