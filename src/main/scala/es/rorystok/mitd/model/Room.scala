package es.rorystok.mitd.model

case class RoomConnection(
  id: RoomConnectionId,
  toRoom: RoomId,
  hidden: Boolean
)

case class Room(
  name: String,
  id: RoomId,
  entrance: Boolean,
  connections: Seq[RoomConnection]
)