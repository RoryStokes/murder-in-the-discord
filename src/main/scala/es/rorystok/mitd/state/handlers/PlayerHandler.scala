package es.rorystok.mitd.state.handlers

import diode.ActionResult.NoChange
import diode.{ActionHandler, Circuit}
import es.rorystok.mitd.model._
import es.rorystok.mitd.state.GameAction.{CreatePlayers, PlayerMoved}

trait PlayerHandler { self: Circuit[GameState] =>

//  def setPlayerMoving(player: Player, to: RoomId): Either[String, Player] = player.location match {
//    case loc: InSingleRoom =>
//      val moving = Moving(loc.roomId, to)
//      Right(player.copy(location = moving))
//    case moving: Moving => Left(s"User is already [$moving], cannot move to [$to]")
//  }
//
//  def getPlayer(players: Seq[Player], userId: UserId): Either[String, (Int, Player)] =
//    Option(players.indexWhere(_.userId == userId))
//      .filter(_ >= 0)
//      .map(i => (i, players(i)))
//      .toRight(_ => s"Cannot find user with id [$userId] exists")

  def logError(error: String) = {
    println(error)
    NoChange
  }

  val playerHandler = new ActionHandler(zoomTo(_.players)) {
    override def handle = {
      case CreatePlayers(players) =>
        updated(players)
    }
  }
}