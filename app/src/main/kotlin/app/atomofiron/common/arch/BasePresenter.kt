package app.atomofiron.common.arch

import android.os.Bundle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope

abstract class BasePresenter<M : ViewModel, R : BaseRouter> constructor(
    protected val scope: CoroutineScope,
    protected val router: R,
    private val nullableResponseRecipient: String? = null,
) : Recipient {
    protected val responseRecipient: String get() = nullableResponseRecipient!!

    abstract fun onSubscribeData()

    open fun onNavigationClick() = router.navigateBack()

    open fun setRecipient(recipient: String) = Unit

    open fun onSaveInstanceState(outState: Bundle) = Unit

    open fun onCleared() = Unit
}