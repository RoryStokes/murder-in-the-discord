package es.rorystok.mitd.game

import es.rorystok.mitd.discord.DiscordService
import es.rorystok.mitd.model.{Room, RoomConnection}
import es.rorystok.mitd.state.GameAction.CreateRooms
import es.rorystok.mitd.state.GameCircuit

import scala.concurrent.Future
import scala.language.implicitConversions
import scala.concurrent.ExecutionContext.Implicits.global

class RoomManager(circuit: GameCircuit){
  import RoomManager._

  private val guild = circuit.zoom(_.guild)

  def init(discordService: DiscordService): Future[Unit] = {
    for {
      _ <- discordService.setVoiceChannels(rooms.map(_.name), hidden = true)
      _ = circuit.dispatch(CreateRooms(rooms))
    } yield ()
  }
}

case class RoomConfig(name: String) {
  val id: String = name

  def ~(other: RoomConfig): Seq[(String, RoomConnection)] = {
    val connId = s"$name~${other.name}"
    Seq(
      id -> RoomConnection(connId, other.id, hidden=false),
      other.id -> RoomConnection(connId, id, hidden=false)
    )
  }
}

object RoomManager {
  val lobby = RoomConfig("Lobby")
  val mainHall = RoomConfig("Main Hall")
  val eastWing = RoomConfig("East Wing")
  val westWing = RoomConfig("West Wing")
  val library = RoomConfig("Library")
  val study = RoomConfig("Study")
  val lounge = RoomConfig("Lounge")
  val kitchen = RoomConfig("Kitchen")

  val roomConfig = Seq(
    lobby,
    mainHall,
    westWing,
    eastWing,
    library,
    study,
    lounge,
    kitchen
  )

  val passages: Seq[(String, RoomConnection)] = Seq(
    lobby ~ mainHall,
    westWing ~ mainHall,
    mainHall ~ eastWing,
    eastWing ~ library,
    eastWing ~ study,
    westWing ~ lounge,
    westWing ~ kitchen
  ).flatten

  val passageMap: Map[String, Seq[RoomConnection]] = passages
    .groupBy(_._1)
    .mapValues(list => list.map(_._2))

  val rooms: Seq[Room] = roomConfig.map(config => Room(
    name = config.name,
    id = config.id,
    connections = passageMap.getOrElse(config.id, Nil),
    entrance = config.name == "Lobby"
  ))
}