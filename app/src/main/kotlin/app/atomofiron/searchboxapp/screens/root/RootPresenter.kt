package app.atomofiron.searchboxapp.screens.root

import app.atomofiron.common.arch.BasePresenter
import kotlinx.coroutines.CoroutineScope

class RootPresenter(
    scope: CoroutineScope,
    router: RootRouter,
) : BasePresenter<RootViewModel, RootRouter>(scope, router) {

    override fun onSubscribeData() = Unit

    override fun onBack(soft: Boolean) = router.onBack(soft) || super.onBack(soft)
}
