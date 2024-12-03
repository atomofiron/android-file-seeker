package app.atomofiron.searchboxapp.screens.template

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.searchboxapp.di.DaggerInjector
import javax.inject.Inject

class TemplateViewModel : BaseViewModel<TemplateComponent, TemplateFragment, TemplateViewState, TemplatePresenter>() {

    @Inject
    override lateinit var presenter: TemplatePresenter
    @Inject
    override lateinit var viewState: TemplateViewState

    override fun component(view: TemplateFragment) = DaggerTemplateComponent
        .builder()
        .bind(viewModelScope)
        .bind(viewProperty)
        .dependencies(DaggerInjector.appComponent)
        .build().apply {
            inject(this@TemplateViewModel)
        }
}