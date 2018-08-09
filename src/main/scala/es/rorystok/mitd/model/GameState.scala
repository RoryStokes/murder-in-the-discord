package es.rorystok.mitd.model

import net.dv8tion.jda.core.entities.Guild

final case class GameState(
  players: Seq[Player],
  rooms: Seq[Room],
  guild: Option[Guild]
)

object GameState {
  val initialState = GameState(Nil, Nil, None)
}