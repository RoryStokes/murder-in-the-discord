package es.rorystok.mitd

import es.rorystok.mitd.discord.{DiscordListener, DiscordService}
import es.rorystok.mitd.game.GameManager
import es.rorystok.mitd.state.GameAction.DiscordUpdate
import es.rorystok.mitd.state.GameCircuit

object Bot extends App {

  val token = sys.env("DISCORD_TOKEN")

  var circuit = new GameCircuit
  var maybeGame: Option[GameManager] = None

  val commandListener = DiscordListener.onMessageReceived { e =>
    if(e.getMessage.getContentRaw == "!init" || e.getMessage.getContentRaw == "!reset") {
      new DiscordService(e.getGuild).setVoiceChannels(Seq("Lobby"))
    } else if(e.getMessage.getContentRaw == "!start") {
      maybeGame = Some(new GameManager(circuit))
    }
  }

  val circuitListener = DiscordListener.onGenericGuild { event =>
    circuit.dispatch(DiscordUpdate(event.getGuild))
  }

  val listeners = Seq(
    DiscordListener.onReady(_ => println("Ready")),
    commandListener,
    circuitListener
  )

  val discord = DiscordService.start(token, listeners)
}