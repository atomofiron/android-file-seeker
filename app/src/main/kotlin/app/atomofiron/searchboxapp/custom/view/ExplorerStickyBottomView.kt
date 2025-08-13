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

@SuppressLint("ViewConstructor")
class ExplorerStickyBottomView(
    context: Context,
    onclick: (Node) -> Unit,
) : FrameLayout(context) {

    private val cornerRadius = resources.getDimension(R.dimen.explorer_border_corner_radius)
    private val binding = ItemExplorerSeparatorBinding.inflate(LayoutInflater.from(context), this, true)
    private lateinit var item: Node

    private var movedTop = 0
    private var drawBottom = 0f
    private val paint = Paint()

    init {
        binding.root.setOnClickListener { onclick(item) }
        paint.style = Paint.Style.FILL
        paint.color = context.findColorByAttr(R.attr.colorBackground)
        setWillNotDraw(false)
        noClip()
    }

    override fun draw(canvas: Canvas) {
        val left = -left.toFloat()
        val right = (parent as View).width + left
        val bottom = height + drawBottom
        canvas.drawRect(left, cornerRadius, right, bottom, paint)
        super.draw(canvas)
    }

    fun bind(item: Node) {
        binding.title.text = item.name
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (top != movedTop) {
            val height = bottom - top
            move(movedTop + height, drawBottom)
        }
    }

    fun move(top: Int, drawBottom: Float) {
        this.drawBottom = drawBottom
        movedTop = top
        val bottom = movedTop + height
        this.top = movedTop
        this.bottom = bottom
        invalidate()
    }
}