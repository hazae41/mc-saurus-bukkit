package json

import com.google.gson.JsonObject
import org.bukkit.Bukkit
import org.bukkit.World
import java.util.*

fun World.toJson() =
  JsonObject().apply {
    addProperty("name", name)
    addProperty("uuid", uid.toString())
  }

fun JsonObject.toWorld(): World {
  val name = get("name").asString
  val uuid = get("uuid").asString
    .let(UUID::fromString)

  return Bukkit.getWorld(uuid)
    ?: Bukkit.getWorld(name)
    ?: throw Exception("Invalid world")
}