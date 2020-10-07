import com.google.gson.Gson
import com.google.gson.JsonObject
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class Channels {
  var hello: String? = null
  var events: String? = null
}

class Saurus : JavaPlugin() {
  var channels = Channels()
  var session: WebSocketSession? = null

  override fun onEnable() {
    super.onEnable()
    this.saveDefaultConfig()

    server.pluginManager.registerEvents(Handler(this), this)

    val password = File(dataFolder, "password.txt").run {
      if (!exists()) throw Exception("No password.txt file")
      readText()
    }

    val host = this.config.getString("host")
    val port = this.config.getInt("port")

    GlobalScope.launch(IO) {
      connect("wss://$host:$port", password)
    }
  }

  fun parse(frame: Frame): JsonObject {
    val text = frame as? Frame.Text
      ?: throw Exception("Invalid frame type");

    val clz = JsonObject::class.java
    val data = Gson().fromJson(text.readText(), clz)

    return data
  }

  fun WebSocketSession.onmessage(frame: Frame) {
    val msg = parse(frame)
    val uuid = msg.get("uuid").asString
    val type = msg.get("type").asString
    val channel = WSChannel(this, uuid)

    server.scheduler.runTask(this@Saurus) { _ ->
      if (type == "open") {
        val path = msg.get("path").asString
        val data = msg.get("data") ?: null;

        val openEvent = ChannelOpenEvent(channel, path, data)
        server.pluginManager.callEvent(openEvent)
      }

      if (type == "other") {
        val data = msg.get("data") ?: null;

        val messageEvent = ChannelMessageEvent(channel, data)
        server.pluginManager.callEvent(messageEvent)
      }

      if (type == "close") {
        val data = msg.get("data") ?: null;

        val messageEvent = ChannelMessageEvent(channel, data)
        server.pluginManager.callEvent(messageEvent)

        val closeEvent = ChannelCloseEvent(channel, "OK")
        server.pluginManager.callEvent(closeEvent)
      }

      if (type == "error") {
        val reason = msg.get("reason").asString

        val closeEvent = ChannelCloseEvent(channel, reason)
        server.pluginManager.callEvent(closeEvent)
      }
    }
  }

  suspend fun WebSocketSession.connected(password: String) {
    session = this;

    server.scheduler.runTask(this@Saurus) { _ ->
      val e = OpenEvent()
      server.pluginManager.callEvent(e)
    }

    WSChannel(this).open("/hello", JsonObject().apply {
      addProperty("type", "server")
      addProperty("platform", "bukkit")
      addProperty("password", password)
    })

    for (frame in incoming)
      onmessage(frame)

    server.scheduler.runTask(this@Saurus) { _ ->
      val e = CloseEvent()
      server.pluginManager.callEvent(e)
    }

    channels = Channels()
    session = null;
  }

  suspend fun connect(url: String, password: String) {
    val client = HttpClient(CIO).config {
      install(WebSockets)
    }

    while (true) {
      try {
        client.wss(url) { connected(password) }
      } catch (e: Exception) {
        logger.warning(e.message)
        delay(5000)
      }
    }
  }

}