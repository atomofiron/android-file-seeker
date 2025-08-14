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
import app.atomofiron.fileseeker.databinding.ItemExplorerSeparatorBinding
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.makeSeparator
import app.atomofiron.searchboxapp.screens.explorer.fragment.roots.RootViewHolder.Companion.getTitle

@SuppressLint("ViewConstructor")
class ExplorerStickyBottomView(
    context: Context,
    onclick: (Node) -> Unit,
) : FrameLayout(context) {

    private val binding = ItemExplorerSeparatorBinding.inflate(LayoutInflater.from(context), this, true)
    private lateinit var item: Node

    private var drawRange: IntRange? = null
    private val paint = Paint()

    init {
        binding.makeSeparator()
        binding.root.setOnClickListener { onclick(item) }
        paint.style = Paint.Style.FILL
        paint.color = context.findColorByAttr(R.attr.colorBackground)
        setWillNotDraw(false)
        noClip()
    }

    override fun draw(canvas: Canvas) {
        drawRange?.let {
            val left = -left.toFloat()
            val top = it.first - translationY
            val right = (parent as View).width + left
            val bottom = it.last - translationY
            canvas.drawRect(left, top, right, bottom, paint)
        }
        super.draw(canvas)
    }

    fun bind(item: Node) {
        this.item = item
        binding.title.text = item.getTitle(resources)
    }

    fun move(offset: Int, drawRange: IntRange?) {
        this.drawRange = drawRange
        translationY = offset.toFloat()
        invalidate()
    }
}