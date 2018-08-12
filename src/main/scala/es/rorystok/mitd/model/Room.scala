package es.rorystok.mitd.model

case class RoomConnection(
  id: RoomConnectionId,
  toRoom: String,
  hidden: Boolean
)

case class Room(
  name: String,
  channelId: ChannelId,
  entrance: Boolean,
  connections: Seq[RoomConnection]
) {
  lazy val ref = RoomRef(name, channelId)
}

case class RoomRef(name: String, channelId: ChannelId) {
  override def toString: String = s"r[$name]"
}