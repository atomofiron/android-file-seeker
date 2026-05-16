package app.atomofiron.common.arch

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import app.atomofiron.searchboxapp.model.other.AppScreen
import kotlin.reflect.KClass

class BaseFragmentImpl<F : Fragment, S : Any, P : BasePresenter<*,*>, B : ViewBinding> : BaseFragment<F,S,P,B> {

    override var screen: AppScreen? = null
        private set
    override lateinit var presenter: P
    override lateinit var viewState: S

    override fun initViewModel(fragment: F, viewModelClass: KClass<out BaseViewModel<*,F,S,P>>, state: Bundle?) {
        val viewModel = ViewModelProvider(fragment)[viewModelClass.java]
        viewModel.setView(fragment)
        screen = viewModel.screen
        presenter = viewModel.presenter
        viewState = viewModel.viewState
        if (state != null) viewModel.onRestoreState(state)
    }
}