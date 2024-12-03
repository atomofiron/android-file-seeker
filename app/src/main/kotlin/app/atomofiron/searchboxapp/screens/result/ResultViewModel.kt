package app.atomofiron.searchboxapp.screens.result

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams
import javax.inject.Inject

class ResultViewModel : BaseViewModel<ResultComponent, ResultFragment, ResultViewState, ResultPresenter>() {

    @Inject
    override lateinit var presenter: ResultPresenter
    @Inject
    override lateinit var viewState: ResultViewState

    override fun component(view: ResultFragment): ResultComponent {
        val params = ResultPresenterParams.params(view.requireArguments())
        return DaggerResultComponent
            .builder()
            .bind(viewProperty)
            .bind(viewModelScope)
            .bind(params)
            .dependencies(DaggerInjector.appComponent)
            .build().apply {
                inject(this@ResultViewModel)
            }
    }
}