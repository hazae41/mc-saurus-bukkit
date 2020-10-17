import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import handlers.PlayerHandler
import json.toPlayer
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class Handler(val saurus: Saurus) : Listener {

  @EventHandler
  fun onClose(e: CloseEvent) {
    saurus.server.onlinePlayers.forEach {
      it.kickPlayer("Disconnected")
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onMessage(e: ChannelOpenEvent) {
    try {
      val path = e.path

      val split = path.split("/")
      val first = split.getOrNull(1)
      val second = split.getOrNull(2)

      if (first == "events")
        handleEvents(e.channel)

      if (first == "execute")
        handleExecute(e.channel, e.data!!)

      if (first == "player")
        handlePlayer(e.channel, e.data!!, second)

    } catch (ex: Exception) {
      GlobalScope.launch(IO) {
        e.channel.error(ex.message)
      }
    }
  }

  fun handleEvents(channel: WSChannel) {
    if (saurus.events !== null)
      throw Exception("Already opened")

    val session = saurus.session!!
    val uuid = channel.uuid

    saurus.events = WSChannel(session, uuid)
  }

  fun handleExecute(channel: WSChannel, data: JsonElement) {
    val server = saurus.server
    val command = data.asString
    val sender = server.consoleSender

    val done = server.dispatchCommand(sender, command)
      .let(::JsonPrimitive)

    GlobalScope.launch(IO) { channel.close(done) }
  }

  fun handlePlayer(channel: WSChannel, data: JsonElement, path: String?) {
    val req = data.asJsonObject

    val player = req.get("player")
      .asJsonObject.toPlayer()

    PlayerHandler(player, channel, req)
      .handle(path)
  }
}