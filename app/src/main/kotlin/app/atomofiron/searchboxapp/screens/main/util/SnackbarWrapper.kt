package app.atomofiron.searchboxapp.screens.main.util

import android.content.Context
import app.atomofiron.common.util.findBooleanByAttr
import com.google.android.material.snackbar.Snackbar
import app.atomofiron.searchboxapp.R

class SnackbarWrapper(
    private val context: Context,
    private val factory: () -> Snackbar
) {
    private var snackbar: Snackbar? = null
    private var isDark = false

    fun show() {
        if (snackbar == null) {
            snackbar = factory()
            isDark = context.findBooleanByAttr(R.attr.isDarkTheme)
        } else {
            val isDark = context.findBooleanByAttr(R.attr.isDarkTheme)
            if (this.isDark xor isDark) {
                this.isDark = isDark
                snackbar = factory()
            }
        }
        snackbar!!.show()
    }
}