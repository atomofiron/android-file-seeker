package app.atomofiron.searchboxapp.screens.template

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import kotlinx.coroutines.CoroutineScope

class TemplatePresenter(
    scope: CoroutineScope,
    router: TemplateRouter,
    preferenceStore: PreferenceStore,
) : BasePresenter<TemplateViewModel, TemplateRouter>(scope, router) {

    override fun onSubscribeData() = Unit
}
