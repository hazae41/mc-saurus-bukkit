package hazae41.saurus.listeners

import com.google.gson.JsonElement
import hazae41.saurus.Saurus
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun Saurus.sendEvent(data: JsonElement?) =
  GlobalScope.launch(IO) { events?.send(data) }