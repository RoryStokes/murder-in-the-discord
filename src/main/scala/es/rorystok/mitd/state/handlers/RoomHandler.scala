package es.rorystok.mitd.state.handlers

import diode.{ActionHandler, ActionResult, Circuit}
import es.rorystok.mitd.game.RoomManager
import es.rorystok.mitd.model.{ChannelId, GameState, Room}
import es.rorystok.mitd.state.GameAction.InitialiseRoom

trait RoomHandler { self: Circuit[GameState] =>

  val roomHandler: ActionHandler[GameState, Map[ChannelId, Room]] = new ActionHandler(zoomTo(_.rooms)) {
    override def handle: PartialFunction[Any, ActionResult[GameState]] = {
      case InitialiseRoom(room) =>
        updated(value + (room.channelId -> RoomManager.getRoom(room)))
    }
  }
}