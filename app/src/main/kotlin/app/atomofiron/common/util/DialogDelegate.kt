package app.atomofiron.common.util

import android.graphics.drawable.Drawable
import androidx.annotation.StringRes
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.other.UniText

typealias DialogButton = Pair<UniText, (checked: Boolean) -> Unit>

interface DialogDelegate {

    fun loadingIcon(): Drawable?

    operator fun get(text: UniText): String

    fun showError(message: UniText? = null)

    infix fun show(config: DialogConfig): DialogUpdater?

    enum class CheckBox(@StringRes val label: Int) {
        DontShowAnymore(R.string.dont_show_anymore),
        RememberMyChoice(R.string.remember_my_choice),
    }

    companion object {
        val Cancel = Cancel { }
        fun Cancel(onClick: (checked: Boolean) -> Unit): DialogButton = UniText(R.string.cancel) to onClick
    }
}
