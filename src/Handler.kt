import com.google.gson.JsonObject
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent

class Handler(val saurus: Saurus) : Listener {

  @EventHandler
  fun onlogin(e: PlayerLoginEvent) {
    if (saurus.session != null) return;
    e.result = PlayerLoginEvent.Result.KICK_OTHER
  }

  @EventHandler
  fun onjoin(e: PlayerJoinEvent) {
    val session = saurus.session ?: return;

    GlobalScope.launch(IO) {
      val data = JsonObject().apply {
        addProperty("type", "player.join")
        add("player", e.player.toJson())
      }

      val msg = msgOf("event", data)
      session.send(msg.toString())
    }
  }

  @EventHandler
  fun ondeath(e: PlayerDeathEvent) {
    val session = saurus.session ?: return;

    GlobalScope.launch(IO) {
      val data = JsonObject().apply {
        addProperty("type", "player.death")
        add("location", e.entity.location.toJson())
        add("player", e.entity.toJson())
      }

      val msg = msgOf("event", data)
      session.send(msg.toString())
    }
  }

  @EventHandler
  fun onquit(e: PlayerQuitEvent) {
    val session = saurus.session ?: return;

    GlobalScope.launch(IO) {
      val data = JsonObject().apply {
        addProperty("type", "player.quit")
        add("player", e.player.toJson())
      }

      val msg = msgOf("event", data)
      session.send(msg.toString())
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
    val session = saurus.session ?: return;

    val logger = saurus.logger
    val server = saurus.server

    logger.info(e.action + e.data.toString())

    val action = e.action
    val split = action.split(".")
    val first = split.getOrNull(0)
    val second = split.getOrNull(1)

    if (first == "execute") {
      val command = e.data.asString
      val sender = server.consoleSender
      val done = server.dispatchCommand(sender, command)

      GlobalScope.launch(IO) {
        val msg = JsonObject().apply {
          addProperty("channel", e.channel)
          addProperty("data", done)
        }

        session.send(msg.toString())
      }
    }

    if (first == "player") {
      val data = e.data.asJsonObject

      val _player = data.get("player").asJsonObject
      val name = _player.get("name").asString
      val player = server.getPlayer(name)
      if (player === null) return

      if (second == "kick") {
        val reason = data.get("reason").asString
        player.kickPlayer(reason)
      }

      if (second == "message") {
        val message = data.get("message").asString
        player.sendMessage(message)
      }

      if (second == "actionbar") {
        val message = data.get("message").asString
        val text = TextComponent.fromLegacyText(message)
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *text)
      }

      if (second == "title") {
        val title = data.get("title").asString
        val subtitle = data.get("subtitle").asString

        val fadein = data.get("fadein")?.asInt ?: 10
        val stay = data.get("stay")?.asInt ?: 70
        val fadeout = data.get("fadeout")?.asInt ?: 20

        player.sendTitle(title, subtitle, fadein, stay, fadeout)
      }
    }

  }
}