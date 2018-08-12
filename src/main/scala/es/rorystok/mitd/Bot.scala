package es.rorystok.mitd

import es.rorystok.mitd.discord.{DiscordListener, DiscordService}
import es.rorystok.mitd.game.GameManager
import es.rorystok.mitd.state.GameAction._
import es.rorystok.mitd.state.GameCircuit

object Bot extends App {

  val token = sys.env("DISCORD_TOKEN")

  var circuit = new GameCircuit
  var maybeGame: Option[GameManager] = None

  val commandListener = DiscordListener.onMessageReceived { e =>
    if(e.getMessage.getContentRaw == "!init" || e.getMessage.getContentRaw == "!reset") {
      DiscordService.setVoiceChannels(Seq("Lobby"), e.getGuild)
    } else if(e.getMessage.getContentRaw == "!start") {
      maybeGame = Some(new GameManager(circuit))
    }
  }

  val circuitListener = DiscordListener.onGenericGuild { event =>
    circuit.dispatch(DiscordUpdate(event.getGuild))
  }

  val listeners = Seq(
    DiscordListener.onReady(_ => println("Ready")),
    DiscordListener.onGuildVoiceMove { event =>
      circuit.dispatch(PlayerMoving(DiscordService.playerRef(event.getMember), DiscordService.roomRef(event.getChannelJoined)))
    },
    DiscordListener.onGuildVoiceJoin { event =>
      circuit.dispatch(PlayerMoving(DiscordService.playerRef(event.getMember), DiscordService.roomRef(event.getChannelJoined)))
    },
    DiscordListener.onGuildVoiceLeave { event =>
      circuit.dispatch(PlayerMissing(DiscordService.playerRef(event.getMember)))
    },
    commandListener,
    circuitListener
  )

  val UserRole = "\\$p_(.*)".r
  val discord = new DiscordService(token, listeners)
}