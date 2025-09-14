package app.atomofiron.searchboxapp.custom.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.widget.FrameLayout
import app.atomofiron.common.util.extension.debugFail
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.noClip
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemExplorerBinding
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl.ExplorerItemBinderActionListener
import app.atomofiron.searchboxapp.utils.attach

@SuppressLint("ViewConstructor")
class ExplorerStickyTopView(
    context: Context,
    listener: ExplorerItemBinderActionListener? = null,
) : FrameLayout(context) {

    private val cornerRadius = resources.getDimension(R.dimen.explorer_border_corner_radius)
    private val binder = ExplorerItemBinderImpl(attach(ItemExplorerBinding::inflate), isOpened = true)

    private var composition: ExplorerItemComposition? = null
    private var item: Node? = null
    private var drawTop = 0f
    private val paint = Paint()

    init {
        binder.setOnItemActionListener(listener)
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
        item ?: return debugFail { "why item is null?" }
        this.item = item
        this.composition = composition ?: this.composition
        binder.bind(item)
        composition?.let { binder.bindComposition(it) }
    }

    fun drawTop(size: Int) {
        drawTop = size.toFloat()
        invalidate()
    }
}