package hazae41.saurus.json

import com.google.gson.JsonElement
import com.google.gson.JsonObject

fun JsonObject.getOrNull(key: String): JsonElement? {
  return if (has(key)) get(key) else null
}