package app.atomofiron.common.recycler

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION

const val FILL_ROW = 0f

open class GeneralHolder<D : Any>(view: View) : RecyclerView.ViewHolder(view) {

    protected val context: Context = view.context
    protected val resources: Resources = view.resources

    protected var itemOrNull: D? = null
        private set
    protected val item: D
        get() = itemOrNull!!

    open val hungry = true
    var truePosition = NO_POSITION
        protected set

    constructor(parent: ViewGroup, layoutId: Int) : this(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))

    fun bind(item: D, position: Int) {
        onPreBind(itemOrNull, item, position)
        truePosition = position
        itemOrNull = item
        onBind(item, position)
    }

    protected open fun onPreBind(old: D?, new: D, position: Int) = Unit

    protected open fun onBind(item: D, position: Int) = Unit

    open fun minWidth() = FILL_ROW
}