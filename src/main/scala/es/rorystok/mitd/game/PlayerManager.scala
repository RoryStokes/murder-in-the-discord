package es.rorystok.mitd.game

import es.rorystok.mitd.discord.DiscordService
import es.rorystok.mitd.model._
import es.rorystok.mitd.state.GameAction.CreatePlayers
import es.rorystok.mitd.state.GameCircuit

import scala.language.implicitConversions

class PlayerManager(circuit: GameCircuit){
  var players: Seq[Player] = Nil

  private val guild = circuit.zoom(_.guild)
  guild() foreach  { g =>
    val discordService = new DiscordService(g)
    val members = discordService.getMembersInRoom("Lobby")
    players = members.map(member => Player(
      name = member.getEffectiveName,
      userId = member.getUser.getId,
      location = Missing("Main Hall")
    ))
    println(players)
    circuit.dispatch(CreatePlayers(players))
    players.foreach(player => discordService.createOrFetchUserRole(player.userId))
  }

  def playersWhoSeeRoom(room: Room): Seq[Player] = {
    players.filter(playerCanSee(_, room))
  }

  def playerCanSee(player: Player, room: Room): Boolean = {
    player.location match {
      case Moving(_, to) => to == room.id
      case loc: InSingleRoom if loc.roomId == room.id => true
      case InRoom(roomId) =>
        room.connections.exists(_.toRoom == roomId)
      case _ => false
    }
  }
}
