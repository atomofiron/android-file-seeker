package app.atomofiron.searchboxapp.screens.licenses

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.searchboxapp.di.DaggerInjector
import javax.inject.Inject

class LicensesViewModel : BaseViewModel<LicensesComponent, LicensesFragment, LicensesViewState, LicensesPresenter>() {

    @Inject
    override lateinit var presenter: LicensesPresenter
    @Inject
    override lateinit var viewState: LicensesViewState

    override fun component(view: LicensesFragment) = DaggerLicensesComponent
        .builder()
        .bind(viewModelScope)
        .bind(viewProperty)
        .dependencies(DaggerInjector.appComponent)
        .build().apply {
            inject(this@LicensesViewModel)
        }
}