package es.rorystok.mitd

import es.rorystok.mitd.model.Room

object Util {
  def idForName(name: String) = name.split("[^\\w]").mkString("")
  def roleForRoom(room: Room) =s"$$r_${idForName(room.name)}"
  def roleForRoom(room: String) =s"$$r_${idForName(room)}"
}
