import com.google.gson.JsonObject
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class OpenEvent() : Event() {
  override fun getHandlers() = OpenEvent.handlers

  companion object {
    @JvmStatic
    fun getHandlerList() = handlers
    private val handlers = HandlerList()
  }
}

class CloseEvent() : Event() {
  override fun getHandlers() = CloseEvent.handlers

  companion object {
    @JvmStatic
    fun getHandlerList() = handlers
    private val handlers = HandlerList()
  }
}

class ChannelOpenEvent(val json: JsonObject) : Event() {
  val channel get() = json.get("channel").asString
  val action get() = json.get("action").asString
  val data get() = json.get("data")

  override fun getHandlers() = ChannelOpenEvent.handlers

  companion object {
    @JvmStatic
    fun getHandlerList() = handlers
    private val handlers = HandlerList()
  }
}

class ChannelMessageEvent(val json: JsonObject) : Event() {
  val channel get() = json.get("channel").asString
  val data get() = json.get("data")

  override fun getHandlers() = ChannelMessageEvent.handlers

  companion object {
    @JvmStatic
    fun getHandlerList() = handlers
    private val handlers = HandlerList()
  }
}

class ChannelCloseEvent(val json: JsonObject) : Event() {
  val channel get() = json.get("channel").asString
  val reason get() = json.get("reason").asString

  override fun getHandlers() = ChannelCloseEvent.handlers

  companion object {
    @JvmStatic
    fun getHandlerList() = handlers
    private val handlers = HandlerList()
  }
}