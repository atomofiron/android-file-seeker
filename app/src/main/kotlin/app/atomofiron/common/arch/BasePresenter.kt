package app.atomofiron.common.arch

import android.os.Bundle
import androidx.lifecycle.ViewModel
import app.atomofiron.searchboxapp.utils.CoroutineLauncher
import kotlinx.coroutines.CoroutineScope

abstract class BasePresenter<M : ViewModel, R : BaseRouter>(
    protected val scope: CoroutineScope,
    protected val router: R,
) : Recipient, CoroutineLauncher by CoroutineLauncher(scope) {

    abstract fun onSubscribeData()

    open fun onNavigationClick() = router.navigateBack()

    open fun setRecipient(recipient: String) = Unit

    open fun onSaveInstanceState(outState: Bundle) = Unit

    open fun onBack(soft: Boolean): Boolean = router.navigateBack()

    open fun onCleared() = Unit
}