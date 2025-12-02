package app.atomofiron.searchboxapp.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.atomofiron.common.util.extension.debugFail
import app.atomofiron.common.util.extension.get
import app.atomofiron.searchboxapp.model.explorer.NodeRef

class DeleteScreenshotReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val ref = intent.get<NodeRef>()
        ref ?: return debugFail { "ref to delete is null" }
        NativeBridge.delete(ref, asSu = false)
    }
}