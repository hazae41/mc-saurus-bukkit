import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player

class PlayerHandler(
  val player: Player,
  val channel: WSChannel,
  val data: JsonObject,
) {
  fun handle(path: String?) {
    if (path == "kick")
      handleKick()

    if (path == "location")
      handleLocation()

    if (path == "teleport")
      handleTeleport()

    if (path == "time")
      handleTime()

    if (path == "message")
      handleMessage()

    if (path == "actionbar")
      handleActionbar()

    if (path == "title")
      handleTitle()

    if (path == "locale")
      handleLocale()

    if (path == "displayName")
      handleDisplayName()
  }

  fun handleKick() {
    val reason = data.get("reason").asString
    player.kickPlayer(reason)

    GlobalScope.launch(IO) { channel.close() }
  }

  fun handleLocation() {
    val location = player.location.toJson()
    GlobalScope.launch(IO) { channel.close(location) }
  }

  fun handleTeleport() {
    val location = data.get("location")
      .asJsonObject.toLocation()
    player.teleport(location)

    GlobalScope.launch(IO) { channel.close() }
  }

  fun handleTime() {
    if (data.has("value")) {
      val time = data.get("value").asLong
      player.setPlayerTime(time, false)

      GlobalScope.launch(IO) { channel.close() }
    } else {
      val time = JsonPrimitive(player.playerTime)

      GlobalScope.launch(IO) { channel.close(time) }
    }
  }

  fun handleMessage() {
    val message = data.get("message").asString
    player.sendMessage(message)

    GlobalScope.launch(IO) { channel.close() }
  }

  fun handleActionbar() {
    val message = data.get("message").asString
    val text = TextComponent.fromLegacyText(message)
    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *text)

    GlobalScope.launch(IO) { channel.close() }
  }

  fun handleTitle() {
    val title = data.get("title").asString
    val subtitle = data.get("subtitle").asString

    val fadein = data.get("fadein")?.asInt ?: 10
    val stay = data.get("stay")?.asInt ?: 70
    val fadeout = data.get("fadeout")?.asInt ?: 20

    player.sendTitle(title, subtitle, fadein, stay, fadeout)

    GlobalScope.launch(IO) { channel.close() }
  }

  fun handleLocale() {
    val locale = JsonPrimitive(player.locale)

    GlobalScope.launch(IO) { channel.close(locale) }
  }

  fun handleDisplayName() {
    if (data.has("value")) {
      val displayName = data.get("value").asString
      player.setDisplayName(displayName)

      GlobalScope.launch(IO) { channel.close() }
    } else {
      val displayName = JsonPrimitive(player.displayName)

      GlobalScope.launch(IO) { channel.close(displayName) }
    }
  }
}
