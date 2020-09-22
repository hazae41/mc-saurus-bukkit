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

class Saurus : JavaPlugin() {
  val handler = Handler(this)
  var session: WebSocketSession? = null

  var events = ""

  override fun onEnable() {
    super.onEnable()
    dataFolder.mkdir()

    server.pluginManager.registerEvents(handler, this);

    val password = File(dataFolder, "password.txt").run {
      if (!exists()) createNewFile()
      readText()
    }

    GlobalScope.launch(IO) {
      connect("wss://sunship.tk:25564", password)
    }
  }

  fun onmessage(frame: Frame) {
    val data = parse(frame)
    val method = data.get("method").asString

    server.scheduler.runTask(this@Saurus) { _ ->
      if (method == "open") {
        val e = ChannelOpenEvent(data)
        server.pluginManager.callEvent(e)
      }
      if (method == "none") {
        val e = ChannelMessageEvent(data)
        server.pluginManager.callEvent(e)
      }
      if (method == "close") {
        val e = ChannelCloseEvent(data)
        server.pluginManager.callEvent(e)
      }
    }
  }

  fun onopen() {
    server.scheduler.runTask(this@Saurus) { _ ->
      val e = OpenEvent()
      server.pluginManager.callEvent(e)
    }
  }

  fun onclose() {
    server.scheduler.runTask(this@Saurus) { _ ->
      val e = CloseEvent()
      server.pluginManager.callEvent(e)
    }
  }

  fun parse(frame: Frame): JsonObject {
    val text = frame as? Frame.Text
      ?: throw IllegalArgumentException();

    val clz = JsonObject::class.java
    val data = Gson().fromJson(text.readText(), clz)

    return data
  }

  suspend fun WebSocketSession.sendHello(password: String) {
    val hello = msgOf("hello", JsonObject().apply {
      addProperty("type", "server")
      addProperty("platform", "bukkit")
      addProperty("password", password)
    })

    send(hello.toString())
  }

  suspend fun WebSocketSession.recvHello() {
    val input = parse(incoming.receive())

    val channel = input.get("channel").asString
    println("channel: $channel")

    val data = input.get("data").asJsonObject
    val uuid = data.get("uuid").asNumber
    println("uuid: $uuid")
  }

  suspend fun WebSocketSession.onconnect() {
    session = this;
    onopen()

    for (frame in incoming)
      onmessage(frame)

    onclose()
    session = null;
  }

  suspend fun connect(url: String, password: String) {
    val client = HttpClient(CIO).config {
      install(WebSockets)
    }

    while (true) {
      try {
        client.wss(url) {
          logger.info("Connected")
          sendHello(password)
          recvHello()
          onconnect()
          logger.info("Disconnected")
        }
      } catch (e: Exception) {
        logger.warning(e.message)
        delay(5000)
      }
    }
  }

}