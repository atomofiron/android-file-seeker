package app.atomofiron.searchboxapp.screens.main

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.searchboxapp.di.DaggerInjector
import javax.inject.Inject

class MainViewModel : BaseViewModel<MainComponent, MainActivity, MainViewState, MainPresenter>() {

    @Inject
    override lateinit var presenter: MainPresenter
    @Inject
    override lateinit var viewState: MainViewState

    override fun component(view: MainActivity) = DaggerMainComponent
        .builder()
        .bind(viewProperty)
        .bind(viewModelScope)
        .dependencies(DaggerInjector.appComponent)
        .build().apply {
            inject(this@MainViewModel)
        }
}