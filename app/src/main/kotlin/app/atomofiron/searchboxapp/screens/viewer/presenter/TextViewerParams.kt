package app.atomofiron.searchboxapp.screens.viewer.presenter

import android.os.Bundle
import app.atomofiron.common.util.extension.get
import app.atomofiron.common.util.extension.put
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
class TextViewerParams(
    val ref: NodeRef,
    val length: ULong,
    val initialTaskId: Uuid?,
    val charset: String,
) {
    companion object {

        fun arguments(
            ref: NodeRef,
            length: ULong,
            charset: String,
            taskId: Uuid? = null,
        ) = Bundle().put(TextViewerParams(ref, length, taskId, charset))

        fun params(arguments: Bundle) = arguments.get<TextViewerParams>()!!
    }
}
