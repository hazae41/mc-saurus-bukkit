package hazae41.saurus.json

import com.google.gson.JsonObject
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

fun Player.toJson() =
  JsonObject().apply {
    addProperty("name", name)
    addProperty("uuid", uniqueId.toString())
  }

fun JsonObject.toPlayer(): Player {
  val name = get("name").asString
  val uuid = get("uuid").asString
    .let(UUID::fromString)

  return Bukkit.getPlayer(uuid)
    ?: Bukkit.getPlayer(name)
    ?: throw Exception("Invalid player")
}