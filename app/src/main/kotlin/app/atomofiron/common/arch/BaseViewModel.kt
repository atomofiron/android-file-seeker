package app.atomofiron.common.arch

import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import app.atomofiron.common.util.property.MutableWeakProperty

/* ViewModel has screen's lifecycle,
then builds di component (and register Android-side things),
and it doesn't do anything other and shouldn't */
abstract class BaseViewModel<D : Any, V : LifecycleOwner, S : Any, P : BasePresenter<*,*>> : ViewModel() {

    val viewProperty: MutableWeakProperty<V> = MutableWeakProperty()

    abstract val presenter: P
    abstract val viewState: S
    private lateinit var componentRef: D
    open val registerable: Registerable? = null

    open fun setView(view: V) {
        viewProperty.value = view
        if (!::componentRef.isInitialized) {
            componentRef = component(view)
        }
        registerable?.register()
    }

    abstract fun component(view: V): D

    open fun onSaveState(state: Bundle) = Unit

    open fun onRestoreState(state: Bundle) = Unit

    override fun onCleared() {
        super.onCleared()
        presenter.onCleared()
    }
}