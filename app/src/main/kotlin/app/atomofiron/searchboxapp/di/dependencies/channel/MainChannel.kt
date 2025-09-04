package app.atomofiron.searchboxapp.di.dependencies.channel

import android.net.Uri
import app.atomofiron.common.util.flow.EventFlow

class MainChannel {
    val fileToReceive = EventFlow<Uri>()
    val maximized = EventFlow<Unit>()
}