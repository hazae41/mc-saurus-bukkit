import com.google.gson.JsonElement
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

class ChannelOpenEvent(
  val channel: WSChannel,
  val path: String,
  val data: JsonElement?
) : Event() {
  override fun getHandlers() = ChannelOpenEvent.handlers

  companion object {
    @JvmStatic
    fun getHandlerList() = handlers
    private val handlers = HandlerList()
  }
}

class ChannelMessageEvent(
  val channel: WSChannel,
  val data: JsonElement?
) : Event() {
  override fun getHandlers() = ChannelMessageEvent.handlers

  companion object {
    @JvmStatic
    fun getHandlerList() = handlers
    private val handlers = HandlerList()
  }
}

class ChannelCloseEvent(
  val channel: WSChannel,
  val reason: String
) : Event() {
  override fun getHandlers() = ChannelCloseEvent.handlers

  companion object {
    @JvmStatic
    fun getHandlerList() = handlers
    private val handlers = HandlerList()
  }
}