package listeners

import Saurus
import json.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.weather.WeatherChangeEvent

class WeatherListener(val saurus: Saurus) : Listener {
  @EventHandler(priority = EventPriority.MONITOR)
  fun onChange(e: WeatherChangeEvent) {
    val event = Event("weather.change")
      .apply { addProperty("raining", e.toWeatherState()) }

    saurus.sendEvent(event)
  }
}