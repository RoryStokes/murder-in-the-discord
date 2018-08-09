package es.rorystok.mitd.state.handlers

import diode.ActionResult.NoChange
import diode.{ActionHandler, Circuit}
import es.rorystok.mitd.model.GameState
import es.rorystok.mitd.state.GameAction.DiscordUpdate
import net.dv8tion.jda.core.entities.Guild

trait DiscordHandler { self: Circuit[GameState] =>
  def writeGuild(state: GameState, guild: Option[Guild]) = {
    Option(state).getOrElse(GameState.initialState).copy(guild = guild)
  }
  val guildRW = zoomRW(_.guild)(writeGuild)


  val discordHandler: ActionHandler[GameState, Option[Guild]] = new ActionHandler(guildRW) {
    override def handle = {
      case DiscordUpdate(guild) => updated(Option(guild))
    }
  }
}