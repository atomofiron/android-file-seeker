package app.atomofiron.searchboxapp.screens.main.fragment

import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

class SnackbarCallbackFragmentDelegate(
    private val output: SnackbarCallbackOutput
) : BaseTransientBottomBar.BaseCallback<Snackbar>() {

    override fun onShown(transientBottomBar: Snackbar?) = output.onShown()

    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) = output.onDismissed()

    interface SnackbarCallbackOutput {
        var isExitSnackbarShown: Boolean

        fun onShown() {
            isExitSnackbarShown = true
        }

        fun onDismissed() {
            isExitSnackbarShown = false
        }
    }
}