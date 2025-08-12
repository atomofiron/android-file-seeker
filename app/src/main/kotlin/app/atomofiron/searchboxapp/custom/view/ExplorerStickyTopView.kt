package app.atomofiron.searchboxapp.custom.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.noClip
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemExplorerBinding
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.makeDeepest
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.makeOpened
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl.ExplorerItemBinderActionListener

@SuppressLint("ViewConstructor")
class ExplorerStickyTopView(
    context: Context,
    deepest: Boolean? = null,
    listener: ExplorerItemBinderActionListener? = null,
) : FrameLayout(context) {

    private val cornerRadius = resources.getDimension(R.dimen.explorer_border_corner_radius)
    private val binder = LayoutInflater.from(context).inflate(R.layout.item_explorer, this).run {
        val binding = ItemExplorerBinding.bind(getChildAt(0))
        when (deepest) {
            true -> binding.makeDeepest()
            false -> binding.makeOpened()
            null -> Unit
        }
        ExplorerItemBinderImpl(binding.root)
    }

    private var composition: ExplorerItemComposition? = null
    private var item: Node? = null
    private var movedTop = 0
    private var drawTop = 0f
    private val paint = Paint()

    init {
        binder.onItemActionListener = listener
        paint.style = Paint.Style.FILL
        paint.color = context.findColorByAttr(R.attr.colorBackground)
        setWillNotDraw(false)
        noClip()
    }

    override fun draw(canvas: Canvas) {
        val left = -left.toFloat()
        val right = (parent as View).width + left
        val bottom = height - cornerRadius
        canvas.drawRect(left, -drawTop, right, bottom, paint)
        super.draw(canvas)
    }

    fun bind(
        item: Node? = this.item,
        composition: ExplorerItemComposition? = this.composition,
    ) {
        this.item = item ?: this.item
        this.composition = composition ?: this.composition
        tryBind()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (top != movedTop) {
            val height = bottom - top
            move(movedTop + height, drawTop)
        }
    }

    fun move(top: Int, drawTop: Float) {
        this.drawTop = drawTop
        movedTop = top
        val bottom = movedTop + height
        this.top = movedTop
        this.bottom = bottom
        invalidate()
    }

    private fun tryBind() {
        val item = item ?: return
        binder.onBind(item)
        composition?.let { binder.bindComposition(it) }
    }
}