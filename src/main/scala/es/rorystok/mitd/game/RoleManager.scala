package es.rorystok.mitd.game

import net.dv8tion.jda.core.entities.Guild
import es.rorystok.mitd.Util
import es.rorystok.mitd.model.Room

import collection.JavaConverters._
import scala.language.implicitConversions

class RoleManager(rooms: Seq[Room], passages: Seq[(Room, Room)]) {

  def init(guild: Guild): Unit = {
    guild.getRoles.asScala.filter(_.getName.startsWith("$")).foreach { role =>
      role.delete().complete()
    }

    rooms foreach { room =>
      guild.getController.createRole
        .setName(Util.roleForRoom(room))
        .complete()
    }

    guild.getController.createRole
      .setName("$moving")
      .complete()
  }
}
