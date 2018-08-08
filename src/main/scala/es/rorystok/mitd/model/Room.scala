package es.rorystok.mitd.model

case class Room(name: String) {
  val id = name.split("[^\\w]").mkString("")
}