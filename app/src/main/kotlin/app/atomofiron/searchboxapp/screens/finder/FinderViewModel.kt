package app.atomofiron.searchboxapp.screens.finder

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.common.arch.Registerable
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.model.other.AppScreen
import javax.inject.Inject

class FinderViewModel : BaseViewModel<FinderComponent, FinderFragment, FinderViewState, FinderPresenter>() {

    override val screen = AppScreen.Finder

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