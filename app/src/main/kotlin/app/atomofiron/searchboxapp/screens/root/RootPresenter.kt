package app.atomofiron.searchboxapp.screens.root

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.searchboxapp.di.dependencies.delegate.ScreenDelegate
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@RootScope
class RootPresenter @Inject constructor(
    scope: CoroutineScope,
    router: RootRouter,
    private val screenDelegate: ScreenDelegate,
) : BasePresenter<RootViewModel, RootRouter>(scope, router)
    , ScreenDelegate by screenDelegate
{

    override fun onSubscribeData() = Unit

    override fun onBack(soft: Boolean) = router.onBack(soft) || super.onBack(soft)
}
