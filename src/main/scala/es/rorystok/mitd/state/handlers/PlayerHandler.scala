package es.rorystok.mitd.state.handlers

import diode.ActionResult.{EffectOnly, ModelUpdate, ModelUpdateEffect, NoChange}
import diode._
import es.rorystok.mitd.discord.DiscordAction
import es.rorystok.mitd.game.RoomManager
import es.rorystok.mitd.model._
import es.rorystok.mitd.state.GameAction._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import diode.Implicits.runAfterImpl
import es.rorystok.mitd.discord.DiscordAction.SetPlayerAccess

trait PlayerHandler { self: Circuit[GameState] =>

  def logError(error: String): ActionResult.NoChange.type = {
    println(error)
    NoChange
  }

  val playerHandler: ActionHandler[GameState, Map[UserId, Player]] = new ActionHandler(zoomTo(_.players)) {
    override def handle: PartialFunction[Any, ActionResult[GameState]] = {
      case CreatePlayers(players) => updated(players.map(p => p.userId -> p).toMap)
      case update: PlayerUpdate =>
        value.get(update.player.userId) map { player =>
          val rootState = modelRW.root.value
          val result = handlePlayerUpdate(player)(update)
          result match {
            case PlayerUpdateResult(None, None) => noChange
            case PlayerUpdateResult(None, Some(effect)) => effectOnly(effect)
            case PlayerUpdateResult(Some(newLoc), maybeEffect) =>
              val newRooms = newLoc match {
                case InRoom(room) => RoomManager.getConnectedRooms(room, player, rootState.rooms)
                case Moving(_, toRoom) => Seq(toRoom)
                case Missing(room) => Seq(room)
              }

              val updateAccess = Effect.action(SetPlayerAccess(player, newRooms))

              val updatedPlayer = player.copy(location = newLoc, canSee = newRooms)
              val updatedMap = value.updated(update.player.userId, updatedPlayer)

              maybeEffect match {
                case None => updated(updatedMap, updateAccess)
                case Some(effect) => updated(updatedMap, updateAccess + effect)
              }
          }
        } getOrElse NoChange
    }

    private def roomFromLocation(location: PlayerLocation): RoomRef = location match {
      case InRoom(ref) => ref
      case Missing(ref) => ref
      case Moving(_, ref) => ref
    }

    case class PlayerUpdateResult(newLocation: Option[PlayerLocation], effect: Option[Effect] = None)

    def handlePlayerUpdate(player: Player): PlayerUpdate => PlayerUpdateResult = {
      case PlayerMoving(userId, to) =>
        player.location match {
          case InRoom(from) =>
            val scheduleMoved = runAfter(2.seconds)(PlayerMoved(userId, to))
            val deafenPlayer = Effect.action(DiscordAction.SetPlayerDeafened(player.ref))
            PlayerUpdateResult(Some(Moving(from, to)), Some(deafenPlayer + scheduleMoved))
          case Missing(from) if from == to =>
            PlayerUpdateResult(Some(InRoom(from)))
          case location =>
            PlayerUpdateResult(None, Some(Effect.action(
              DiscordAction.MovePlayerToChannel(player.ref, roomFromLocation(location))
            )))
        }
      case PlayerMoved(_, to) => PlayerUpdateResult(
        Some(InRoom(to)),
        Some(Effect.action(DiscordAction.SetPlayerDeafened(player.ref, false)))
      )
      case PlayerMissing(_) =>
        player.location match {
          case loc: InSingleRoom => PlayerUpdateResult(Some(Missing(loc.room)))
          case Moving(from, _) => PlayerUpdateResult(Some(Missing(from)))
          case _ => PlayerUpdateResult(None)
        }
    }
  }
}