package app.atomofiron.common.arch

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import kotlin.reflect.KClass


interface BaseFragment<F : Fragment, S : Any, P : BasePresenter<*,*>, B : ViewBinding> {
    val viewState: S
    val presenter: P
    val isLightStatusBar: Boolean? get() = null

    fun initViewModel(fragment: F, viewModelClass: KClass<out BaseViewModel<*,F,S,P>>, state: Bundle?)
    fun onBack(): Boolean = presenter.onBack()

    // reminders
    fun S.onViewCollect() = Unit
    fun B.onApplyInsets() = Unit

    val Fragment.isTopVisible: Boolean get() = parentFragmentManager.fragments.findLast { it.isVisible } === this
}