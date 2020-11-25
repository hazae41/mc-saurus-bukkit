package hazae41.saurus.listeners

import hazae41.saurus.Saurus
import hazae41.saurus.json.PlayerEvent
import hazae41.saurus.json.toJson
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*

class PlayerListener(val saurus: Saurus) : Listener {

  @EventHandler(priority = EventPriority.NORMAL)
  fun onCommand(e: PlayerCommandPreprocessEvent) {
    if (!e.message.startsWith("/! ")) return
    val command = e.message.substring("/! ".length)

    e.isCancelled = true

    val event = PlayerEvent("player.command", e.player)
      .apply { addProperty("command", command) }

    saurus.sendEvent(event)
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onJoin(e: PlayerJoinEvent) {
    val event = PlayerEvent("player.join", e.player)
      .apply {
        add("location", e.player.location.toJson())
        addProperty("message", e.joinMessage)
      }

    saurus.sendEvent(event)
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onDeath(e: PlayerDeathEvent) {
    val event = PlayerEvent("player.death", e.entity)
      .apply {
        add("location", e.entity.location.toJson())
        addProperty("message", e.deathMessage)
      }

    saurus.sendEvent(event)
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onQuit(e: PlayerQuitEvent) {
    val event = PlayerEvent("player.quit", e.player)
      .apply {
        add("location", e.player.location.toJson())
        addProperty("message", e.quitMessage)
      }

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