package json

import com.google.gson.JsonObject
import org.bukkit.Location

fun Location.toJson() =
  JsonObject().apply {
    addProperty("x", x)
    addProperty("y", y)
    addProperty("z", z)
    add("world", world?.toJson())
  }

fun JsonObject.toLocation(): Location {
  val x = get("x").asDouble
  val y = get("y").asDouble
  val z = get("z").asDouble

  val worldInfo =
    if (!has("world")) null
    else get("world").asJsonObject

  val world = worldInfo?.toWorld()

  return Location(world, x, y, z)
}