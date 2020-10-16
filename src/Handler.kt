import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*

class Handler(val saurus: Saurus) : Listener {

  @EventHandler
  fun onlogin(e: PlayerLoginEvent) {
    if (saurus.session != null) return;
    e.result = PlayerLoginEvent.Result.KICK_OTHER
    e.kickMessage = "Server is not ready"
  }

  @EventHandler
  fun onclose(e: CloseEvent) {
    saurus.server.onlinePlayers.forEach {
      it.kickPlayer("Disconnected")
    }
  }

  @EventHandler(priority = EventPriority.NORMAL)
  fun oncommand(e: PlayerCommandPreprocessEvent) {
    val split = e.message.split(" ")
    if (split[0] != "/!") return
    e.isCancelled = true

    val code = split[1]

    GlobalScope.launch(IO) {
      saurus.events?.send(
        PlayerEvent("player.code", e.player).apply {
          addProperty("code", code)
        }
      )
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onjoin(e: PlayerJoinEvent) {
    GlobalScope.launch(IO) {
      saurus.events?.send(
        PlayerEvent("player.join", e.player).apply {
          addProperty("message", e.joinMessage)
        }
      )
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun ondeath(e: PlayerDeathEvent) {
    GlobalScope.launch(IO) {
      saurus.events?.send(
        PlayerEvent("player.death", e.entity).apply {
          addProperty("message", e.deathMessage)
        }
      )
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onquit(e: PlayerQuitEvent) {
    GlobalScope.launch(IO) {
      saurus.events?.send(
        PlayerEvent("player.quit", e.player).apply {
          addProperty("message", e.quitMessage)
        }
      )
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onrespawn(e: PlayerRespawnEvent) {
    GlobalScope.launch(IO) {
      saurus.events?.send(
        PlayerEvent("player.respawn", e.player).apply {
          add("location", e.respawnLocation.toJson())
          addProperty("anchor", e.isAnchorSpawn)
          addProperty("bed", e.isBedSpawn)
        }
      )
    }
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onchat(e: AsyncPlayerChatEvent) {
    GlobalScope.launch(IO) {
      saurus.events?.send(
        PlayerEvent("player.chat", e.player).apply {
          addProperty("format", e.format)
          addProperty("message", e.message)
        }
      )
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

  fun handlePlayer(channel: WSChannel, _data: JsonElement, path: String?) {
    val data = _data.asJsonObject

    val _player = data.get("player").asJsonObject
    val _name = _player.get("name").asString

    val player = saurus.server.getPlayer(_name)
      ?: throw Exception("Invalid player")

    PlayerHandler(player, channel, data)
      .handle(path)
  }

  @EventHandler
  fun onmessage(e: ChannelOpenEvent) {
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
}