package handlers

import WSChannel
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import getOrNull
import json.toJson
import json.toLocation
import json.toTeleportCause
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

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

    if (path == "health")
      handleHealth()

    if (path == "flying")
      handleFlying()

    if (path == "sneaking")
      handleSneaking()

    if (path == "address")
      handleAddress()
  }

  fun handleKick() {
    val reason = data.getOrNull("reason")?.asString
    player.kickPlayer(reason)

    GlobalScope.launch(IO) { channel.close() }
  }

  fun handleLocation() {
    val location = player.location.toJson()
    GlobalScope.launch(IO) { channel.close(location) }
  }

  fun handleTeleport() {
    if (data.has("location")) {
      val location = data.get("location")
        .asJsonObject.toLocation()

      val cause = data.getOrNull("cause")
        ?.asString?.toTeleportCause()

      if (cause !== null) {
        player.teleport(location, cause)
      } else {
        player.teleport(location)
      }

      GlobalScope.launch(IO) { channel.close() }
    }

    if (data.has("entity")) {
      val uuid = data.get("entity").asString
        .let(UUID::fromString)

      val entity = Bukkit.getEntity(uuid)
        ?: throw Exception("Invalid entity")

      val cause = data.getOrNull("cause")
        ?.asString?.toTeleportCause()

      if (cause !== null) {
        player.teleport(entity, cause)
      } else {
        player.teleport(entity)
      }

      GlobalScope.launch(IO) { channel.close() }
    }
  }

  fun handleTime() {
    if (data.has("time")) {
      val time = data.get("time").asLong
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
    if (data.has("displayName")) {
      val displayName = data.get("displayName").asString
      player.setDisplayName(displayName)

      GlobalScope.launch(IO) { channel.close() }
    } else {
      val displayName = JsonPrimitive(player.displayName)
      GlobalScope.launch(IO) { channel.close(displayName) }
    }
  }

  fun handleHealth() {
    if (data.has("health")) {
      player.health = data.get("health").asDouble
      GlobalScope.launch(IO) { channel.close() }
    } else {
      val health = JsonPrimitive(player.health)
      GlobalScope.launch(IO) { channel.close(health) }
    }
  }

  fun handleFlying() {
    if (data.has("flying")) {
      player.isFlying = data.get("flying").asBoolean
      GlobalScope.launch(IO) { channel.close() }
    } else {
      val flying = JsonPrimitive(player.isFlying)
      GlobalScope.launch(IO) { channel.close(flying) }
    }
  }

  fun handleSneaking() {
    if (data.has("sneaking")) {
      player.isSneaking = data.get("sneaking").asBoolean
      GlobalScope.launch(IO) { channel.close() }
    } else {
      val sneaking = JsonPrimitive(player.isSneaking)
      GlobalScope.launch(IO) { channel.close(sneaking) }
    }
  }

  fun handleAddress() {
    val address = JsonObject().apply {
      addProperty("hostname", player.address!!.hostName)
      addProperty("port", player.address!!.port)
    }

    GlobalScope.launch(IO) { channel.close(address) }
  }
}
