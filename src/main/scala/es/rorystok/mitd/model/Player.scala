package es.rorystok.mitd.model

case class Player(
  name: String,
  userId: UserId,
  location: PlayerLocation
)

sealed trait PlayerLocation
trait InSingleRoom {
  def roomId: RoomId
}
case class InRoom(roomId: RoomId) extends PlayerLocation with InSingleRoom
case class Missing(roomId: RoomId) extends PlayerLocation with InSingleRoom
case class Moving(from: RoomId, to: RoomId) extends PlayerLocation