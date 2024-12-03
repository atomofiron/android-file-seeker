package app.atomofiron.searchboxapp.screens.viewer

import androidx.lifecycle.viewModelScope
import app.atomofiron.common.arch.BaseViewModel
import app.atomofiron.searchboxapp.di.DaggerInjector
import app.atomofiron.searchboxapp.screens.viewer.presenter.TextViewerParams
import javax.inject.Inject

class TextViewerViewModel : BaseViewModel<TextViewerComponent, TextViewerFragment, TextViewerViewState, TextViewerPresenter>() {
    companion object {
        const val UNDEFINED = -1
    }

    @Inject
    override lateinit var presenter: TextViewerPresenter
    @Inject
    override lateinit var viewState: TextViewerViewState

    override fun component(view: TextViewerFragment): TextViewerComponent {
        val params = TextViewerParams.params(view.requireArguments())
        return DaggerTextViewerComponent
            .builder()
            .bind(viewProperty)
            .bind(viewModelScope)
            .bind(params)
            .dependencies(DaggerInjector.appComponent)
            .build().apply {
                inject(this@TextViewerViewModel)
            }
    }
}