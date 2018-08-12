package es.rorystok.mitd.state.handlers

import diode.{ActionHandler, ActionResult, Circuit, ModelRW}
import es.rorystok.mitd.discord.{DiscordAction, DiscordService}
import es.rorystok.mitd.model.GameState
import es.rorystok.mitd.state.GameAction.DiscordUpdate
import net.dv8tion.jda.core.entities.Guild

trait DiscordHandler { self: Circuit[GameState] =>
  def writeGuild(state: GameState, guild: Option[Guild]): GameState = {
    Option(state).getOrElse(GameState.initialState).copy(guild = guild)
  }
  val guildRW: ModelRW[GameState, Option[Guild]] = zoomRW(_.guild)(writeGuild)

  val discordHandler: ActionHandler[GameState, Option[Guild]] = new ActionHandler(guildRW) {
    override def handle: PartialFunction[Any, ActionResult[GameState]] = {
      case DiscordUpdate(guild) => updated(Option(guild))
      case action: DiscordAction => value.flatMap(action.execute) match {
        case Some(effect) => effectOnly(effect)
        case _ => noChange
      }
    }
  }
}