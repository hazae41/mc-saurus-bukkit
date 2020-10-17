package listeners

import Saurus
import com.google.gson.JsonElement
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun Saurus.sendEvent(data: JsonElement?) =
  GlobalScope.launch(IO) { events?.send(data) }