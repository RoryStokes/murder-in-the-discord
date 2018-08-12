package es.rorystok.mitd.game

import es.rorystok.mitd.discord.DiscordService
import es.rorystok.mitd.model._
import es.rorystok.mitd.state.GameAction.CreatePlayers
import es.rorystok.mitd.state.GameCircuit

import scala.language.implicitConversions

class PlayerManager(circuit: GameCircuit){
  var players: Seq[Player] = Nil

  private val guild = circuit.zoom(_.guild)
  for {
    guild <- guild()
    (room, members) <- DiscordService.getMembersInRoom("Lobby", guild)
    players = members.map(member => Player(
      name = member.getEffectiveName,
      userId = member.getUser.getId,
      location = InRoom(room),
      canSee = Seq(room)
    ))
  } yield circuit.dispatch(CreatePlayers(players))
//
//  def playerCanSee(player: Player, room: Room): Boolean = {
//    player.location match {
//      case Moving(_, to) => to == room.id
//      case loc: InSingleRoom if loc.roomId == room.id => true
//      case InRoom(roomId) =>
//        room.connections.exists(_.toRoom == roomId)
//      case _ => false
//    }
//  }
}
