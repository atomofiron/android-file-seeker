package app.atomofiron.common.recycler

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView

open class GeneralHolder<D : Any>(view: View) : RecyclerView.ViewHolder(view) {

    protected val context: Context = view.context

    protected var itemOrNull: D? = null
        private set
    protected open val item: D
        get() = itemOrNull!!

    open val hungry = true

    constructor(parent: ViewGroup, layoutId: Int) : this(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))

    fun bind(item: D, position: Int = -1) {
        itemOrNull = item
        onBind(item, position)
    }

    fun <T : View> requireViewById(@IdRes id: Int): T = itemView.findViewById(id)!!

    protected open fun onBind(item: D, position: Int) = Unit

    open fun minWidth() = 0f
}