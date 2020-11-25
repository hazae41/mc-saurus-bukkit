package hazae41.saurus

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import io.ktor.http.cio.websocket.*
import java.util.*

class Channel(
  val session: WebSocketSession,
  val uuid: String = UUID.randomUUID().toString()
) {
  suspend fun open(path: String, data: JsonElement? = null) {
    val uuid = this.uuid

    val msg = JsonObject().apply {
      addProperty("uuid", uuid)
      addProperty("type", "open")
      addProperty("path", path)
      if (data !== null) add("data", data)
    }

    session.send(msg.toString())
  }

  suspend fun send(data: JsonElement? = null) {
    val uuid = this.uuid

    val msg = JsonObject().apply {
      addProperty("uuid", uuid)
      if (data !== null) add("data", data)
    }

    session.send(msg.toString())
  }

  suspend fun close(data: JsonElement? = null) {
    val uuid = this.uuid

    val msg = JsonObject().apply {
      addProperty("uuid", uuid)
      addProperty("type", "close")
      if (data !== null) add("data", data)
    }

    session.send(msg.toString())
  }

  suspend fun error(reason: String? = null) {
    val uuid = this.uuid

    val msg = JsonObject().apply {
      addProperty("uuid", uuid)
      addProperty("type", "error")
      addProperty("reason", reason)
    }

    session.send(msg.toString())
  }
}