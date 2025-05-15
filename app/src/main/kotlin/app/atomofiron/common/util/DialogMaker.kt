package app.atomofiron.common.util

import android.graphics.drawable.Drawable
import androidx.annotation.StringRes
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.other.UniText

typealias DialogMakerButton = Pair<UniText, (checked: Boolean) -> Unit>

interface DialogMaker {

    operator fun get(text: UniText): String

    fun showError(message: String)

    fun show(
        icon: Drawable? = null,
        title: UniText? = null,
        message: UniText? = null,
        withCheckbox: CheckBox? = null,
        cancelable: Boolean = false,
        onCancel: (() -> Unit)? = null,
        onDismiss: (() -> Unit)? = null,
        neutral: DialogMakerButton? = null,
        negative: DialogMakerButton? = null,
        positive: UniText = UniText(R.string.ok),
        onPositiveClick: (checked: Boolean) -> Unit = { },
    )

    enum class CheckBox(@StringRes val label: Int) {
        DontShowAnymore(R.string.dont_show_anymore),
        RememberMyChoice(R.string.remember_my_choice),
    }

    companion object {
        fun cancel(onClick: (checked: Boolean) -> Unit): DialogMakerButton = UniText(R.string.cancel) to onClick
    }
}
