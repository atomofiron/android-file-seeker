package app.atomofiron.searchboxapp.screens.viewer.presenter

import android.os.Bundle
import app.atomofiron.common.util.extension.get
import app.atomofiron.common.util.extension.getUuid
import app.atomofiron.common.util.extension.put
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import kotlin.uuid.Uuid

class TextViewerParams(
    val ref: NodeRef,
    val initialTaskId: Uuid?,
) {
    companion object {

        fun arguments(ref: NodeRef, taskId: Uuid? = null) = Bundle().apply {
            put(ref)
            if (taskId != null) put(taskId)
        }

        fun params(arguments: Bundle): TextViewerParams {
            return TextViewerParams(arguments.get<NodeRef>()!!, arguments.getUuid())
        }
    }
}
