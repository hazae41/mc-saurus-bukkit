package hazae41.saurus.json

import com.google.gson.JsonObject
import org.bukkit.entity.Player

fun Event(event: String) =
  JsonObject().apply {
    addProperty("event", event)
  }

fun PlayerEvent(event: String, player: Player) =
  Event(event).apply {
    add("player", player.toJson())
  }