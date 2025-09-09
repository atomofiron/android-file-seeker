package app.atomofiron.common.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION

open class GeneralHolder<D : Any>(view: View) : RecyclerView.ViewHolder(view) {

    protected val context: Context = view.context

    private var _itemOrNull: D? = null
    protected open val itemOrNull: D?
        get() = _itemOrNull
    protected open val item: D
        get() = _itemOrNull!!

    open val hungry = true
    var trueBindingAdapterPosition = NO_POSITION
        private set

    constructor(parent: ViewGroup, layoutId: Int) : this(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))

    fun bind(item: D, position: Int) {
        trueBindingAdapterPosition = position
        _itemOrNull = item
        onBind(item, position)
    }

    protected open fun onBind(item: D, position: Int) = Unit

    open fun minWidth() = 0f
}