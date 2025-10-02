package app.atomofiron.searchboxapp.screens.viewer.presenter

import android.os.Bundle
import app.atomofiron.searchboxapp.model.explorer.NodePath
import app.atomofiron.searchboxapp.utils.getSerializableCompat
import java.util.*

class TextViewerParams(
    val path: NodePath,
    val initialTaskId: UUID?,
) {
    companion object {
        private const val KEY_PATH = "KEY_PATH"
        private const val KEY_TASK_ID = "KEY_TASK_ID"

        fun arguments(path: NodePath, taskId: UUID? = null) = Bundle().apply {
            putByteArray(KEY_PATH, path.bytes)
            if (taskId != null) putSerializable(KEY_TASK_ID, taskId)
        }

        fun params(arguments: Bundle): TextViewerParams {
            return TextViewerParams(
                NodePath(arguments.getByteArray(KEY_PATH)!!),
                arguments.getSerializableCompat(KEY_TASK_ID, UUID::class.java),
            )
        }
    }
}
