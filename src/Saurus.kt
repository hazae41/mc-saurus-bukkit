package hazae41.saurus

import com.google.gson.Gson
import com.google.gson.JsonObject
import hazae41.saurus.listeners.PlayerListener
import hazae41.saurus.listeners.WeatherListener
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class Saurus : JavaPlugin() {
  var events: WSChannel? = null
  var session: WebSocketSession? = null

  val ping = AtomicBoolean(false)

  override fun onEnable() {
    super.onEnable()
    this.saveDefaultConfig()

    server.pluginManager.registerEvents(Handler(this), this)
    server.pluginManager.registerEvents(PlayerListener(this), this)
    server.pluginManager.registerEvents(WeatherListener(this), this)

    val password = File(dataFolder, "password.txt").run {
      if (!exists()) throw Exception("No password.txt file")
      readText()
    }

    val name = this.config.getString("name")
      ?: throw Exception("config.name is null")

    val host = this.config.getString("host")
      ?: throw Exception("config.host is null")

    val port = this.config.getInt("port", 8443)

    GlobalScope.launch(IO) {
      connect(host, port, name, password)
    }
  }

  suspend fun WebSocketSession.onmessage(frame: Frame) {
    if (frame is Frame.Ping) {
      ping.set(true)
      send(Frame.Pong(frame.data))
      return;
    }

    val text = frame as? Frame.Text ?: return;

    val clz = JsonObject::class.java
    val msg = Gson().fromJson(text.readText(), clz)

    server.scheduler.runTask(this@Saurus) { _ ->
      val uuid = msg.get("uuid").asString
      val channel = WSChannel(this, uuid)

      if (!msg.has("type")) {
        val data = msg.get("data") ?: null;

        val messageEvent = ChannelMessageEvent(channel, data)
        server.pluginManager.callEvent(messageEvent)
      }

      val type = msg.get("type").asString

      if (type == "open") {
        val path = msg.get("path").asString
        val data = msg.get("data") ?: null;

        val openEvent = ChannelOpenEvent(channel, path, data)
        server.pluginManager.callEvent(openEvent)
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

  suspend fun WebSocketSession.connected(
    name: String,
    password: String
  ) {
    session = this;

    server.scheduler.runTask(this@Saurus) { _ ->
      val e = SaurusOpenEvent()
      server.pluginManager.callEvent(e)
    }

    WSChannel(this).open("/hello", JsonObject().apply {
      addProperty("type", "server")
      addProperty("name", name)
      addProperty("platform", "bukkit")
      addProperty("password", password)
    })

    for (frame in incoming)
      onmessage(frame)

    server.scheduler.runTask(this@Saurus) { _ ->
      val e = SaurusCloseEvent()
      server.pluginManager.callEvent(e)
    }

    events = null
    session = null;
  }

  @KtorExperimentalAPI
  suspend fun connect(
    host: String,
    port: Int,
    name: String,
    password: String
  ) {
    val client = HttpClient(CIO) {
      install(WebSockets)
    }

    while (true) {
      try {
        client.wss(HttpMethod.Get, host, port) {
          logger.info("Connected")
          connected(name, password)
          logger.warning("Disconnected")
        }
      } catch (e: Exception) {
        logger.warning(e.message)
        delay(5000)
      }
    }
  }

}