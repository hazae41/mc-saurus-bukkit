import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.bukkit.Location
import org.bukkit.entity.Player

fun Player.toJson() = JsonObject().apply {
  addProperty("name", name)
  addProperty("uuid", uniqueId.toString())
}

fun Location.toJson() = JsonObject().apply {
  addProperty("x", blockX)
  addProperty("y", blockY)
  addProperty("z", blockZ)
}

fun msgOf(channel: String, data: JsonElement) = JsonObject().apply {
  addProperty("channel", channel)
  addProperty("method", "none")
  add("data", data)
}