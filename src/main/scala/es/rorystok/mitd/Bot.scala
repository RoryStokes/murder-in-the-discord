package es.rorystok.mitd

import es.rorystok.mitd.discord.{DiscordListener, DiscordService}
import es.rorystok.mitd.game.{RoleManager, RoomManager}
import es.rorystok.mitd.model.Room

object Bot extends App {

  val token = sys.env("DISCORD_TOKEN")

  val mainHall = Room("Main Hall")
  val eastWing = Room("East Wing")
  val westWing = Room("West Wing")
  val library = Room("Library")
  val study = Room("Study")
  val lounge = Room("Lounge")
  val kitchen = Room("Kitchen")

  val rooms = Seq(
    mainHall,
    westWing,
    eastWing,
    library,
    study,
    lounge,
    kitchen
  )

  val passages = Seq(
    westWing -> mainHall,
    mainHall -> eastWing,
    eastWing -> library,
    eastWing -> study,
    westWing -> lounge,
    westWing -> kitchen
  )


  val roleManager = new RoleManager(rooms, passages)
  val roomManager = new RoomManager(rooms, passages)

  val listeners = Seq(
    DiscordListener.onReady(_ => println("Ready")),
    DiscordListener.onMessageReceived { e =>
      println(e.getMessage.getContentRaw)
      if(e.getMessage.getContentRaw == "!start") {
        roleManager.init(e.getGuild)
        roomManager.init(e.getGuild)
      }
    },
    roomManager
  )

  val discord = DiscordService(token, listeners)
}