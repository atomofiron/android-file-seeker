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
    message: String?,
    onCopyClick: (message: String) -> Unit,
): DialogConfig {
    val text = message?.takeIf { it.isNotEmpty() }
    return DialogConfig(
        cancelable = false,
        title = if (text == null) UniText(R.string.unknown_error) else UniText(R.string.error),
        message = UniText(text),
        neutral = text?.let { UniText(R.string.copy) to { onCopyClick(text) } },
    )
}
