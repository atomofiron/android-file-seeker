package app.atomofiron.common.util.dialog

import android.graphics.drawable.Drawable
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.other.UniText

data class DialogConfig(
    val icon: Drawable? = null,
    val title: UniText? = null,
    val message: UniText? = null,
    val withCheckbox: DialogDelegate.CheckBox? = null,
    val cancelable: Boolean,
    val onCancel: (() -> Unit)? = null,
    val onDismiss: (() -> Unit)? = null,
    val neutral: DialogButton? = null,
    val negative: DialogButton? = null,
    val positive: UniText = UniText(R.string.ok),
    val onPositiveClick: (checked: Boolean) -> Unit = { },
)

fun errorDialogConfig(
    message: UniText?,
    many: Boolean = false,
    onCopyClick: (message: UniText) -> Unit,
): DialogConfig {
    val message = message?.takeIf { it.isNotEmpty }
    return DialogConfig(
        cancelable = false,
        title = when {
            message == null -> UniText(R.string.unknown_error)
            many -> UniText(R.string.errors)
            else -> UniText(R.string.error)
        },
        message = message,
        neutral = message?.let { UniText(R.string.copy) to { onCopyClick(message) } },
    )
}
