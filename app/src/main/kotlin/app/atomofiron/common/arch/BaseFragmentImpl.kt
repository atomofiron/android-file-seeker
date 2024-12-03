package app.atomofiron.common.arch

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import kotlin.reflect.KClass

class BaseFragmentImpl<F : Fragment, S : Any, P : BasePresenter<*,*>> : BaseFragment<F,S,P> {

    override lateinit var presenter: P
    override lateinit var viewState: S

    override fun initViewModel(fragment: F, viewModelClass: KClass<out BaseViewModel<*,F,S,P>>, state: Bundle?) {
        val viewModel = ViewModelProvider(fragment)[viewModelClass.java]
        viewModel.setView(fragment)
        presenter = viewModel.presenter
        viewState = viewModel.viewState
        if (state != null) viewModel.onRestoreState(state)
    }
}