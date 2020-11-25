package hazae41.saurus

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerLoginEvent.Result.KICK_OTHER
import org.bukkit.event.server.ServerListPingEvent

class Strict(val saurus: Saurus) : Listener {

  @EventHandler(priority = EventPriority.MONITOR)
  fun onPing(e: ServerListPingEvent) {
    if (saurus.session != null) return;
    if (!saurus.config.strict) return
    e.motd = "Server is starting..."
  }

  @EventHandler
  fun onLogin(e: PlayerLoginEvent) {
    if (saurus.session != null) return;
    if (!saurus.config.strict) return
    e.disallow(KICK_OTHER, "Server is starting...")
  }

  @EventHandler(priority = EventPriority.MONITOR)
  fun onClose(e: SaurusCloseEvent) {
    if (!saurus.config.strict) return

    saurus.server.onlinePlayers.forEach {
      it.kickPlayer("Disconnected")
    }
  }

}