package app.atomofiron.common.arch

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import kotlin.reflect.KClass

interface BaseFragment<F : Fragment, S : Any, P : BasePresenter<*,*>> {
    val viewState: S
    val presenter: P
    val isLightStatusBar: Boolean? get() = null

    fun initViewModel(fragment: F, viewModelClass: KClass<out BaseViewModel<*,F,S,P>>, state: Bundle?)
    fun onBack(): Boolean = false

    // reminders
    fun S.onViewCollect() = Unit
    fun onApplyInsets(root: View) = Unit

    val Fragment.isTopVisible: Boolean get() = parentFragmentManager.fragments.findLast { it.isVisible } === this
}