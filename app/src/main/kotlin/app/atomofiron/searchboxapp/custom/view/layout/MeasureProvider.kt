package app.atomofiron.searchboxapp.custom.view.layout

import android.view.View.MeasureSpec
import android.view.ViewGroup

typealias MeasureListener = (width: Int, height: Int) -> Unit

interface MeasureProvider {
    val view: ViewGroup
    val availableWidth: Int

    fun addMeasureListener(listener: MeasureListener)
    fun removeMeasureListener(listener: MeasureListener): Boolean
    fun onPreMeasure(widthSpec: Int, heightSpec: Int)
}

class MeasureProviderImpl : MeasureProvider {

    override val view: ViewGroup get() = throw NotImplementedError()

    override var availableWidth = 0
        private set
    private var availableHeight = 0
    private var measured = false
    private var listeners = mutableListOf<MeasureListener>()

    override fun addMeasureListener(listener: MeasureListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
            if (measured) listener(availableWidth, availableHeight)
        }
    }

    override fun removeMeasureListener(listener: MeasureListener) = listeners.remove(listener)

    override fun onPreMeasure(widthSpec: Int, heightSpec: Int) {
        this.availableWidth = MeasureSpec.getSize(widthSpec)
        this.availableHeight = MeasureSpec.getSize(heightSpec)
        measured = true
        listeners.forEach { it(availableWidth, availableHeight) }
    }
}
