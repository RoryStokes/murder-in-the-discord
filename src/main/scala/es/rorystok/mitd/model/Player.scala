package es.rorystok.mitd.model

case class Player(
  name: String,
  userId: UserId,
  location: PlayerLocation,
  canSee: Seq[RoomRef] = Nil
) {
  lazy val ref = PlayerRef(name, userId)
}

case class PlayerRef(name: String, userId: UserId) {
  override def toString: String = s"p[$name]"
}

sealed trait PlayerLocation
trait InSingleRoom {
  def room: RoomRef
}
case class InRoom(room: RoomRef) extends PlayerLocation with InSingleRoom
case class Missing(room: RoomRef) extends PlayerLocation with InSingleRoom
case class Moving(from: RoomRef, to: RoomRef) extends PlayerLocation