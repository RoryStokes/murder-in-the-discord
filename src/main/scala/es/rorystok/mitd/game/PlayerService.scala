package es.rorystok.mitd.game

import es.rorystok.mitd.discord.DiscordService
import es.rorystok.mitd.model._
import es.rorystok.mitd.state.GameAction.CreatePlayers
import es.rorystok.mitd.state.{GameAction, GameCircuit}
import net.dv8tion.jda.core.entities.Guild

import scala.language.implicitConversions

object PlayerService {
  def initialisePlayers(guild: Guild): Option[GameAction] = {
    for {
      (room, members) <- DiscordService.getMembersInRoom("Lobby", guild)
      players = members.map(member => Player(
        name = member.getEffectiveName,
        userId = member.getUser.getId,
        location = InRoom(room),
        canSee = Seq(room)
      ))
    } yield CreatePlayers(players)
  }
}
