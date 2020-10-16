import com.google.gson.JsonObject
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player

fun Event(event: String) =
  JsonObject().apply {
    addProperty("event", event)
  }

fun PlayerEvent(event: String, player: Player) =
  Event(event).apply {
    add("location", player.location.toJson())
    add("player", player.toJson())
  }

fun Player.toJson() =
  JsonObject().apply {
    addProperty("name", name)
    addProperty("uuid", uniqueId.toString())
  }

fun JsonObject.toLocation(): Location {
  val x = get("x").asDouble
  val y = get("y").asDouble
  val z = get("z").asDouble

  val worldName =
    if (!has("world")) null
    else get("world").asString

  val world = worldName?.let(Bukkit::getWorld)

  return Location(world, x, y, z)
}

fun Location.toJson() =
  JsonObject().apply {
    addProperty("x", x)
    addProperty("y", y)
    addProperty("z", z)
    addProperty("world", world?.name)
  }