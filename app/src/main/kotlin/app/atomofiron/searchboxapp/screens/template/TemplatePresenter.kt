package app.atomofiron.searchboxapp.screens.template

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@TemplateScope
class TemplatePresenter @Inject constructor(
    scope: CoroutineScope,
    viewState: TemplateViewState,
    router: TemplateRouter,
    preferences: PreferenceStore,
) : BasePresenter<TemplateViewModel, TemplateRouter>(scope, router) {

    override fun onSubscribeData() = Unit
}
