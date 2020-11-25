package hazae41.saurus

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import hazae41.saurus.handlers.PlayerHandler
import hazae41.saurus.json.toPlayer
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerLoginEvent.Result.KICK_OTHER
import org.bukkit.event.server.ServerListPingEvent

class Handler(val saurus: Saurus) : Listener {

  @EventHandler(priority = EventPriority.MONITOR)
  fun onPing(e: ServerListPingEvent) {
    e.motd = "Server is starting..."
  }

  @EventHandler
  fun onLogin(e: PlayerLoginEvent) {
    if (saurus.session != null) return;
    e.disallow(KICK_OTHER, "Server is starting...")
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onClose(e: SaurusCloseEvent) {
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