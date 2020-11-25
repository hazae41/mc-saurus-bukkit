package hazae41.saurus.json

import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause.*

fun TeleportCause.toJson(): String {
  if (this == COMMAND)
    return "command"
  if (this == PLUGIN)
    return "plugin"
  if (this == UNKNOWN)
    return "unknown"
  if (this == NETHER_PORTAL)
    return "nether-portal"
  if (this == END_PORTAL)
    return "end-portal"
  if (this == END_GATEWAY)
    return "end-gateway"
  if (this == ENDER_PEARL)
    return "ender-pearl"
  if (this == CHORUS_FRUIT)
    return "chorus-fruit"
  if (this == SPECTATE)
    return "spectate"
  throw Exception("Invalid cause")
}

fun String.toTeleportCause(): TeleportCause? {
  if (this == "command")
    return COMMAND
  if (this == "plugin")
    return PLUGIN
  if (this == "unknown")
    return UNKNOWN
  if (this == "nether-portal")
    return NETHER_PORTAL
  if (this == "end-portal")
    return END_PORTAL
  if (this == "end-gateway")
    return END_GATEWAY
  if (this == "ender-pearl")
    return ENDER_PEARL
  if (this == "chorus-fruit")
    return CHORUS_FRUIT
  if (this == "spectate")
    return SPECTATE
  throw Exception("Invalid cause")
}