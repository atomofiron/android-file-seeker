package app.atomofiron.common.arch

import android.os.Bundle
import androidx.lifecycle.ViewModel
import app.atomofiron.common.util.property.MutableWeakProperty

abstract class BaseViewModel<D : Any, V : Any, S : Any, P : BasePresenter<*,*>> : ViewModel() {

    val viewProperty: MutableWeakProperty<V> = MutableWeakProperty()

    abstract val presenter: P
    abstract val viewState: S
    private lateinit var componentRef: D

    open fun setView(view: V) {
        viewProperty.value = view
        if (!::componentRef.isInitialized) {
            componentRef = component(view)
        }
    }

    abstract fun component(view: V): D

    open fun onSaveState(state: Bundle) = Unit

    open fun onRestoreState(state: Bundle) = Unit

    override fun onCleared() {
        super.onCleared()
        presenter.onCleared()
    }
}