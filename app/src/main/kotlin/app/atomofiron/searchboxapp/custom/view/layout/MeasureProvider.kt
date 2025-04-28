package app.atomofiron.searchboxapp.custom.view.layout

import android.view.View
import android.view.View.MeasureSpec

typealias MeasureListener = (width: Int, height: Int) -> Unit

interface MeasureProvider {
    val view: View

    fun addMeasureListener(listener: MeasureListener)
    fun removeMeasureListener(listener: MeasureListener): Boolean
    fun onPreMeasure(widthSpec: Int, heightSpec: Int)
}

class MeasureProviderImpl : MeasureProvider {

    override val view: View get() = throw NotImplementedError()

    private var availableWidth = 0
    private var availableHeight = 0
    private var listeners = mutableListOf<MeasureListener>()

    override fun addMeasureListener(listener: MeasureListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
            listener(availableWidth, availableHeight)
        }
    }

    override fun removeMeasureListener(listener: MeasureListener) = listeners.remove(listener)

    override fun onPreMeasure(widthSpec: Int, heightSpec: Int) {
        this.availableWidth = MeasureSpec.getSize(widthSpec)
        this.availableHeight = MeasureSpec.getSize(heightSpec)
        listeners.forEach { it(availableWidth, availableHeight) }
    }
}
