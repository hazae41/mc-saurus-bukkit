package hazae41.saurus.listeners

import hazae41.saurus.Saurus
import hazae41.saurus.json.Event
import hazae41.saurus.json.toJson
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.weather.WeatherChangeEvent

class WeatherListener(val saurus: Saurus) : Listener {
  @EventHandler(priority = EventPriority.MONITOR)
  fun onChange(e: WeatherChangeEvent) {
    val event = Event("weather.change").apply {
      add("world", e.world.toJson())
      addProperty("raining", e.toWeatherState())
    }

    saurus.sendEvent(event)
  }
}