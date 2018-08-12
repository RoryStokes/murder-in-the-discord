package es.rorystok.mitd.game

import es.rorystok.mitd.discord.DiscordAction
import es.rorystok.mitd.model._

import scala.language.implicitConversions

object RoomService {

  def initialiseRooms: Seq[DiscordAction] = {
    roomConfig map { config =>
      val visible = config.name == "Main Hall"
      DiscordAction.CreateOrFetchVoiceChannel(config.name, visible)
    }
  }

  case class RoomConfig(name: String) {

    def ~(other: RoomConfig): Seq[(String, RoomConnection)] = {
      val connId = s"$name~${other.name}"
      Seq(
        name -> RoomConnection(connId, other.name, hidden=false),
        other.name -> RoomConnection(connId, name, hidden=false)
      )
    }
  }
  def getRoom(ref: RoomRef) = Room(
    name = ref.name,
    channelId = ref.channelId,
    entrance = ref.name == "Lobby",
    connections = passageMap.getOrElse(ref.name, Nil)
  )

  def findByName(roomName: String, rooms: Map[ChannelId, Room]): Option[Room] =
    rooms.values.find(_.name == roomName)

  def getConnectedRooms(ref: RoomRef, player: Player, rooms: Map[ChannelId, Room]): Seq[RoomRef] = {
    rooms.get(ref.channelId).toSeq flatMap { room =>
      val connected = room.connections.flatMap(conn => findByName(conn.toRoom, rooms)) :+ room
      connected.map(_.ref)
    }
  }

  val mainHall = RoomConfig("Main Hall")
  val eastWing = RoomConfig("East Wing")
  val westWing = RoomConfig("West Wing")
  val library = RoomConfig("Library")
  val study = RoomConfig("Study")
  val lounge = RoomConfig("Lounge")
  val kitchen = RoomConfig("Kitchen")

  val roomConfig = Seq(
    mainHall,
    westWing,
    eastWing,
    library,
    study,
    lounge,
    kitchen
  )

  val passages: Seq[(String, RoomConnection)] = Seq(
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

}