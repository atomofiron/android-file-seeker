package app.atomofiron.searchboxapp.screens.root

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.searchboxapp.di.DaggerInjector
import javax.inject.Inject

class RootViewModel : BaseViewModel<RootComponent, RootFragment, RootViewState, RootPresenter>() {

    @Inject
    override lateinit var presenter: RootPresenter
    @Inject
    override lateinit var viewState: RootViewState

    override fun component(view: RootFragment) = DaggerRootComponent
        .builder()
        .bind(viewModelScope)
        .bind(viewProperty)
        .dependencies(DaggerInjector.appComponent)
        .build().apply {
            inject(this@RootViewModel)
        }
}