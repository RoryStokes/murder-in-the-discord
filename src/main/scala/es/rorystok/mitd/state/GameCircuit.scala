package es.rorystok.mitd.state

import diode.{ActionHandler, ActionResult, Circuit}
import es.rorystok.mitd.model.{GameState, Room}
import es.rorystok.mitd.state.handlers.{DiscordHandler, PlayerHandler, RoomHandler}

class GameCircuit extends Circuit[GameState] with DiscordHandler with RoomHandler with PlayerHandler {
  override val initialModel: GameState = GameState(Nil, Nil, None)

  override val actionHandler: HandlerFunction = composeHandlers(
    discordHandler, roomHandler, playerHandler
  )
}

