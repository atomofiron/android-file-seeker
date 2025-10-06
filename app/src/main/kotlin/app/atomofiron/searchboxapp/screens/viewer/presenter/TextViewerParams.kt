package app.atomofiron.searchboxapp.screens.viewer.presenter

import android.os.Bundle
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.utils.getSerializableCompat
import java.util.*

class TextViewerParams(
    val ref: NodeRef,
    val initialTaskId: UUID?,
) {
    companion object {
        private const val KEY_PATH = "KEY_PATH"
        private const val KEY_TASK_ID = "KEY_TASK_ID"

        fun arguments(ref: NodeRef, taskId: UUID? = null) = Bundle().apply {
            putByteArray(KEY_PATH, ref.bytes)
            if (taskId != null) putSerializable(KEY_TASK_ID, taskId)
        }

        fun params(arguments: Bundle): TextViewerParams {
            return TextViewerParams(
                NodeRef(arguments.getByteArray(KEY_PATH)!!),
                arguments.getSerializableCompat(KEY_TASK_ID, UUID::class.java),
            )
        }
    }
}
