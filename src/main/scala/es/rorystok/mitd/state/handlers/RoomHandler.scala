package es.rorystok.mitd.state.handlers

import diode.ActionResult.NoChange
import diode.{ActionHandler, Circuit}
import es.rorystok.mitd.model.GameState
import es.rorystok.mitd.state.GameAction.{CreateRooms, PlayerMoved}

trait RoomHandler { self: Circuit[GameState] =>

  val roomHandler = new ActionHandler(zoomTo(_.rooms)) {
    override def handle = {
      case CreateRooms(rooms) =>
        println(rooms)
        updated(rooms)
    }
  }
}