package hazae41.saurus

import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerLoginEvent.Result.KICK_OTHER

class Strict(val saurus: Saurus) : Listener {

  @EventHandler(priority = EventPriority.MONITOR)
  fun onPing(e: PaperServerListPingEvent) {
    if (saurus.session != null) return;
    if (!saurus.config.strict) return
    e.isCancelled = true
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