package app.atomofiron.searchboxapp.screens.root

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.util.flow.emitLast
import app.atomofiron.common.util.flow.invoke
import app.atomofiron.common.util.flow.value
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.screens.root.fragment.SnackbarCallbackFragmentDelegate
import app.atomofiron.searchboxapp.screens.root.util.tasks.XTask
import app.atomofiron.searchboxapp.utils.Shell

class RootPresenter(
    private val viewModel: RootViewModel,
    private val router: RootRouter,
    preferenceStore: PreferenceStore
) : SnackbarCallbackFragmentDelegate.SnackbarCallbackOutput {
    override var isExitSnackbarShown: Boolean = false
    private val scope = viewModel.viewModelScope

    init {
        router.showMainIfNeeded()

        preferenceStore.appTheme.collect(scope) {
            viewModel.setTheme.value = it
            router.reattachFragments()
        }
        preferenceStore.appOrientation.collect(scope) {
            viewModel.setOrientation.value = it
        }
        preferenceStore.joystickComposition.collect(scope) {
            viewModel.setJoystick.value = it
        }
        preferenceStore.toyboxVariant.collect(scope) {
            Shell.toyboxPath = it.toyboxPath
        }
        viewModel.tasks.value = Array(16) { XTask() }.toList()
    }

    fun onJoystickClick() = when {
        router.onBack() -> Unit
        else -> viewModel.showExitSnackbar.invoke()
    }

    fun onExitClick() = router.closeApp()

    fun onBackButtonClick() = when {
        router.onBack() -> Unit
        isExitSnackbarShown -> router.closeApp()
        else -> viewModel.showExitSnackbar.invoke()
    }
}