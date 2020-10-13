import com.google.gson.JsonPrimitive
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
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

  @EventHandler(priority = EventPriority.MONITOR)
  fun onmove(e: PlayerMoveEvent) {
    GlobalScope.launch(IO) {
      saurus.events?.send(
        PlayerEvent("player.move", e.player).apply {
          add("from", e.from.toJson())
          add("to", e.to?.toJson())
        }
      )
    }
  }

  @EventHandler
  fun onmessage(e: ChannelOpenEvent) {
    val session = saurus.session ?: return;
    val server = saurus.server

    val path = e.path
    val split = path.split("/")
    val first = split.getOrNull(1)
    val second = split.getOrNull(2)

    if (first == "events") {
      if (saurus.events !== null) return;
      saurus.events = WSChannel(session, e.channel.uuid)
    }

    if (first == "execute") {
      val command = e.data!!.asString
      val sender = server.consoleSender
      val done = server.dispatchCommand(sender, command)

      GlobalScope.launch(IO) {
        e.channel.close(JsonPrimitive(done))
      }
    }

    if (first == "player") {
      val data = e.data!!.asJsonObject

      val _player = data.get("player").asJsonObject
      val _name = _player.get("name").asString

      val player = server.getPlayer(_name)
      if (player === null) return

      if (second == "kick") {
        val reason = data.get("reason").asString
        player.kickPlayer(reason)

        GlobalScope.launch(IO) {
          e.channel.close()
        }
      }

      if (second == "message") {
        val message = data.get("message").asString
        player.sendMessage(message)

        GlobalScope.launch(IO) {
          e.channel.close()
        }
      }

      if (second == "actionbar") {
        val message = data.get("message").asString
        val text = TextComponent.fromLegacyText(message)
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *text)

        GlobalScope.launch(IO) {
          e.channel.close()
        }
      }

      if (second == "title") {
        val title = data.get("title").asString
        val subtitle = data.get("subtitle").asString

        val fadein = data.get("fadein")?.asInt ?: 10
        val stay = data.get("stay")?.asInt ?: 70
        val fadeout = data.get("fadeout")?.asInt ?: 20

        player.sendTitle(title, subtitle, fadein, stay, fadeout)

        GlobalScope.launch(IO) {
          e.channel.close()
        }
      }
    }

  }
}