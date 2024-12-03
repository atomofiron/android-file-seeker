package app.atomofiron.searchboxapp.screens.preferences

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.searchboxapp.di.DaggerInjector
import javax.inject.Inject

class PreferenceViewModel : BaseViewModel<PreferenceComponent, PreferenceFragment, PreferenceViewState, PreferencePresenter>() {

    @Inject
    override lateinit var presenter: PreferencePresenter
    @Inject
    override lateinit var viewState: PreferenceViewState

    override fun component(view: PreferenceFragment) = DaggerPreferenceComponent
        .builder()
        .bind(viewModelScope)
        .bind(viewProperty)
        .dependencies(DaggerInjector.appComponent)
        .build().apply {
            inject(this@PreferenceViewModel)
        }
}