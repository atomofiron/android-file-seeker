package app.atomofiron.searchboxapp.screens.result

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.model.other.AppScreen
import app.atomofiron.searchboxapp.screens.common.activityMode
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams
import javax.inject.Inject

class ResultViewModel : BaseViewModel<ResultComponent, ResultFragment, ResultViewStateTmp, ResultPresenter>() {

    override var screen: AppScreen? = null
    @Inject
    override lateinit var presenter: ResultPresenter
    @Inject
    override lateinit var viewState: ResultViewStateTmp

    override fun component(view: ResultFragment): ResultComponent {
        val params = ResultPresenterParams.params(view.requireArguments())
        screen = AppScreen.Results(params.taskId)
        return DaggerResultComponent
            .builder()
            .bind(viewProperty)
            .bind(viewModelScope)
            .bind(params)
            .bind(view.activityMode)
            .dependencies(DaggerInjector.appComponent)
            .build().apply {
                inject(this@ResultViewModel)
            }
    }
}