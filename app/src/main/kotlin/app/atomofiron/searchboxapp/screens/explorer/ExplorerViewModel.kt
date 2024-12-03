package app.atomofiron.searchboxapp.screens.explorer

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.searchboxapp.di.DaggerInjector
import javax.inject.Inject

class ExplorerViewModel : BaseViewModel<ExplorerComponent, ExplorerFragment, ExplorerViewState, ExplorerPresenter>() {

    @Inject
    override lateinit var viewState: ExplorerViewState
    @Inject
    override lateinit var presenter: ExplorerPresenter

    override fun component(view: ExplorerFragment) = DaggerExplorerComponent
        .builder()
        .bind(viewProperty)
        .bind(viewModelScope)
        .dependencies(DaggerInjector.appComponent)
        .build().apply {
            inject(this@ExplorerViewModel)
        }
}