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

data class Config(
  val name: String,
  val host: String,
  val port: Int,
  val strict: Boolean
)

class Saurus : JavaPlugin() {
  lateinit var config: Config

  var events: Channel? = null
  var session: WebSocketSession? = null

  val ping = AtomicBoolean(false)

  @KtorExperimentalAPI
  override fun onEnable() {
    super.onEnable()

    saveDefaultConfig()

    val name = getConfig().getString("name")
      ?: throw Exception("config.name is null")

    val host = getConfig().getString("host")
      ?: throw Exception("config.host is null")

    val port = getConfig().getInt("port", 8443)

    val strict = getConfig().getBoolean("strict")

    config = Config(name, host, port, strict)

    val password = File(dataFolder, "password.txt").run {
      if (!exists()) throw Exception("No password.txt file")
      readText()
    }

    server.pluginManager.registerEvents(Strict(this), this)
    server.pluginManager.registerEvents(Handler(this), this)
    server.pluginManager.registerEvents(PlayerListener(this), this)
    server.pluginManager.registerEvents(WeatherListener(this), this)

    GlobalScope.launch(IO) { connect(password) }
  }

  @KtorExperimentalAPI
  suspend fun connect(password: String) {
    val client = HttpClient(CIO) {
      install(WebSockets)
    }

    while (true) {
      try {
        client.wss(
          HttpMethod.Get,
          config.host,
          config.port
        ) {
          logger.info("Connected")
          connected(password)
          logger.warning("Disconnected")
        }
      } catch (e: Exception) {
        logger.warning(e.message)
        delay(5000)
      }
    }
  }

  suspend fun WebSocketSession.connected(password: String) {
    session = this;

    server.scheduler.runTask(this@Saurus) { _ ->
      val e = SaurusOpenEvent()
      server.pluginManager.callEvent(e)
    }

    Channel(this).open("/hello", JsonObject().apply {
      addProperty("type", "server")
      addProperty("name", config.name)
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
      val channel = Channel(this, uuid)

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

}