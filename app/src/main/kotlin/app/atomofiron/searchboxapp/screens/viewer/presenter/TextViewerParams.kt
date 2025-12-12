package app.atomofiron.searchboxapp.screens.viewer.presenter

import android.os.Bundle
import app.atomofiron.common.util.extension.get
import app.atomofiron.common.util.extension.getUUID
import app.atomofiron.common.util.extension.put
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import java.util.UUID

class TextViewerParams(
    val ref: NodeRef,
    val initialTaskId: UUID?,
) {
    companion object {

        fun arguments(ref: NodeRef, taskId: UUID? = null) = Bundle().apply {
            put(ref)
            if (taskId != null) put(taskId)
        }

        fun params(arguments: Bundle): TextViewerParams {
            return TextViewerParams(arguments.get<NodeRef>()!!, arguments.getUUID())
        }
    }
}
