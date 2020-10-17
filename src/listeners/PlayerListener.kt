package listeners

import Saurus
import json.PlayerEvent
import json.toJson
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*

class PlayerListener(val saurus: Saurus) : Listener {

  @EventHandler
  fun onLogin(e: PlayerLoginEvent) {
    if (saurus.session != null) return;
    e.result = PlayerLoginEvent.Result.KICK_OTHER
    e.kickMessage = "Server is not ready"
  }

  @EventHandler(priority = EventPriority.NORMAL)
  fun onCommand(e: PlayerCommandPreprocessEvent) {
    val split = e.message.split(" ")
    if (split[0] != "/!") return
    e.isCancelled = true

    val code = split[1]

    val event = PlayerEvent("player.code", e.player)
      .apply { addProperty("code", code) }

    saurus.sendEvent(event)
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onJoin(e: PlayerJoinEvent) {
    val event = PlayerEvent("player.join", e.player)
      .apply { addProperty("message", e.joinMessage) }
    
    saurus.sendEvent(event)
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onDeath(e: PlayerDeathEvent) {
    val event = PlayerEvent("player.death", e.entity)
      .apply { addProperty("message", e.deathMessage) }

    saurus.sendEvent(event)
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onQuit(e: PlayerQuitEvent) {
    val event = PlayerEvent("player.quit", e.player)
      .apply { addProperty("message", e.quitMessage) }

    saurus.sendEvent(event)
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onRespawn(e: PlayerRespawnEvent) {
    val event = PlayerEvent("player.respawn", e.player)
      .apply {
        add("location", e.respawnLocation.toJson())
        addProperty("anchor", e.isAnchorSpawn)
        addProperty("bed", e.isBedSpawn)
      }

    saurus.sendEvent(event)
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onChat(e: AsyncPlayerChatEvent) {
    val event = PlayerEvent("player.chat", e.player)
      .apply {
        addProperty("format", e.format)
        addProperty("message", e.message)
      }

    saurus.sendEvent(event)
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onSneak(e: PlayerToggleSneakEvent) {
    val event = PlayerEvent("player.sneak", e.player)
      .apply { addProperty("sneaking", e.isSneaking) }

    saurus.sendEvent(event)
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onFly(e: PlayerToggleFlightEvent) {
    val event = PlayerEvent("player.fly", e.player)
      .apply { addProperty("flying", e.isFlying) }

    saurus.sendEvent(event)
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onSprint(e: PlayerToggleSprintEvent) {
    val event = PlayerEvent("player.sprint", e.player)
      .apply { addProperty("sprinting", e.isSprinting) }

    saurus.sendEvent(event)
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onTeleport(e: PlayerTeleportEvent) {
    val event = PlayerEvent("player.teleport", e.player)
      .apply {
        addProperty("cause", e.cause.toJson())
        add("from", e.from.toJson())
        add("to", e.to?.toJson())
      }

    saurus.sendEvent(event)
  }
}