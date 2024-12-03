package app.atomofiron.searchboxapp.screens.viewer.presenter

import android.os.Bundle
import app.atomofiron.searchboxapp.screens.viewer.TextViewerFragment
import app.atomofiron.searchboxapp.utils.getSerializableCompat
import java.util.*

class TextViewerParams(
    val path: String,
    val initialTaskId: UUID?,
) {
    companion object {
        private const val KEY_PARAMS = "KEY_PARAMS"
        private const val KEY_TASK_ID = "KEY_TASK_ID"

        fun arguments(path: String, taskId: UUID? = null) = Bundle().apply {
            putString(TextViewerFragment.KEY_PATH, path)
            if (taskId != null) putSerializable(TextViewerFragment.KEY_TASK_ID, taskId)
        }

        fun params(arguments: Bundle): TextViewerParams {
            return TextViewerParams(
                arguments.getString(TextViewerFragment.KEY_PATH)!!,
                arguments.getSerializableCompat(KEY_TASK_ID, UUID::class.java),
            )
        }
    }
}
