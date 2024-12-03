package app.atomofiron.searchboxapp.injectable.channel

import android.net.Uri
import app.atomofiron.common.util.flow.EventFlow

class MainChannel {
    val fileToReceive = EventFlow<Uri>()
    val maximized = EventFlow<Unit>()
}