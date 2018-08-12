package es.rorystok.mitd.state

import diode.{ActionType, Circuit}
import es.rorystok.mitd.model.GameState
import es.rorystok.mitd.state.handlers.{DiscordHandler, PlayerHandler, RoomHandler}

class GameCircuit extends Circuit[GameState] with DiscordHandler with RoomHandler with PlayerHandler {
  override val initialModel: GameState = GameState.initialState

  override val actionHandler: HandlerFunction = foldHandlers(
    discordHandler, roomHandler, playerHandler
  )

  override def dispatch[A](action: A)(implicit evidence$6: ActionType[A]): Unit = {
    println(action)
    super.dispatch(action)
  }
}

