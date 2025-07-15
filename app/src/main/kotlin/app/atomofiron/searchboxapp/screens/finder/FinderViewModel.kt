package app.atomofiron.searchboxapp.screens.finder

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.arch.Registerable
import app.atomofiron.searchboxapp.di.DaggerInjector
import javax.inject.Inject

class FinderViewModel : BaseViewModel<FinderComponent, FinderFragment, FinderViewState, FinderPresenter>() {

    @Inject
    override lateinit var presenter: FinderPresenter
    @Inject
    override lateinit var viewState: FinderViewState
    @Inject
    override lateinit var registerable: Registerable

    override fun component(view: FinderFragment) = DaggerFinderComponent
        .builder()
        .bind(viewModelScope)
        .bind(viewProperty)
        .dependencies(DaggerInjector.appComponent)
        .build().apply {
            inject(this@FinderViewModel)
        }
}