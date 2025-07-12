package app.atomofiron.searchboxapp.screens.finder

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.searchboxapp.di.DaggerInjector
import javax.inject.Inject

class FinderViewModel : BaseViewModel<FinderComponent, FinderFragment, FinderViewState, FinderPresenter>() {

    @Inject
    override lateinit var presenter: FinderPresenter
    @Inject
    override lateinit var viewState: FinderViewState
    @Inject
    lateinit var router: FinderRouter

    override fun setView(view: FinderFragment) {
        super.setView(view)
        router.permissions.register(view)
    }

    override fun component(view: FinderFragment) = DaggerFinderComponent
        .builder()
        .bind(viewModelScope)
        .bind(viewProperty)
        .dependencies(DaggerInjector.appComponent)
        .build().apply {
            inject(this@FinderViewModel)
        }
}