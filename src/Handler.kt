import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent

class Handler(val saurus: Saurus) : Listener {

  @EventHandler
  fun onlogin(e: PlayerLoginEvent) {
    if (saurus.session != null) return;
    e.result = PlayerLoginEvent.Result.KICK_OTHER
    e.kickMessage = "Server is not ready"
  }

  @EventHandler
  fun oncommand(e: PlayerCommandPreprocessEvent) {
    val session = saurus.session ?: return;
    val events = saurus.channels.events ?: return

    val split = e.message.split(" ")
    if (split[0] != "/!") return
    e.isCancelled = true

    val code = split[1]

    GlobalScope.launch(IO) {
      val channel = WSChannel(session, events)
      channel.send(JsonObject().apply {
        addProperty("event", "player.code")
        add("player", e.player.toJson())
        addProperty("code", code)
      })
    }
  }

  @EventHandler
  fun onjoin(e: PlayerJoinEvent) {
    val session = saurus.session ?: return;
    val events = saurus.channels.events ?: return

    GlobalScope.launch(IO) {
      val channel = WSChannel(session, events)
      channel.send(JsonObject().apply {
        addProperty("event", "player.join")
        add("location", e.player.location.toJson())
        add("player", e.player.toJson())
      })
    }
  }

  @EventHandler
  fun ondeath(e: PlayerDeathEvent) {
    val session = saurus.session ?: return;
    val events = saurus.channels.events ?: return

    GlobalScope.launch(IO) {
      val channel = WSChannel(session, events)
      channel.send(JsonObject().apply {
        addProperty("event", "player.death")
        add("location", e.entity.location.toJson())
        add("player", e.entity.toJson())
      })
    }
  }

  @EventHandler
  fun onquit(e: PlayerQuitEvent) {
    val session = saurus.session ?: return;
    val events = saurus.channels.events ?: return

    GlobalScope.launch(IO) {
      val channel = WSChannel(session, events)
      channel.send(JsonObject().apply {
        addProperty("event", "player.quit")
        add("location", e.player.location.toJson())
        add("player", e.player.toJson())
      })
    }
  }

  @EventHandler
  fun onclose(e: CloseEvent) {
    val server = saurus.server

    server.onlinePlayers.forEach {
      it.kickPlayer("Disconnected")
    }
  }

  @EventHandler
  fun onmessage(e: ChannelOpenEvent) {
    val logger = saurus.logger
    val server = saurus.server

    logger.info("${e.path} ${e.data}")

    val path = e.path
    val split = path.split("/")
    val first = split.getOrNull(1)
    val second = split.getOrNull(2)

    if (first == "events") {
      if (saurus.channels.events !== null) return;
      saurus.channels.events = e.channel.uuid
      println("Events channel: ${e.channel.uuid}")
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
      val name = _player.get("name").asString
      val player = server.getPlayer(name)
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