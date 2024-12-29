package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.*
import app.atomofiron.searchboxapp.databinding.ItemExplorerBinding
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.preference.ExplorerItemComposition
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.holder.makeOpenedCurrent
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl
import app.atomofiron.searchboxapp.screens.explorer.fragment.list.util.ExplorerItemBinderImpl.ExplorerItemBinderActionListener
import app.atomofiron.searchboxapp.utils.Alpha

class ExplorerHeaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val binder = LayoutInflater.from(context).inflate(R.layout.item_explorer, this).run {
        val binding = ItemExplorerBinding.bind(getChildAt(0))
        binding.makeOpenedCurrent()
        ExplorerItemBinderImpl(binding.root)
    }

    private var composition: ExplorerItemComposition? = null
    private var item: Node? = null
    private var mTop = 0
    private var insetColor = 0
    private val paint = Paint()

    init {
        var backgroundColor = context.findColorByAttr(R.attr.colorBackground)
        val overlayColor = context.findColorByAttr(R.attr.topRadioGroupBackground)
        backgroundColor = ColorUtils.compositeColors(overlayColor, backgroundColor)
        setBackgroundColor(backgroundColor)
        insetColor = ColorUtils.setAlphaComponent(backgroundColor, Alpha.Level67)
    }

    fun setOnItemActionListener(listener: ExplorerItemBinderActionListener) {
        binder.onItemActionListener = listener
    }

    fun setComposition(composition: ExplorerItemComposition) {
        this.composition = composition
        tryBind()
    }

    fun bind(item: Node?) {
        this.item = item
        tryBind()
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (top != mTop) {
            val height = bottom - top
            move(mTop + height)
        }
    }

    fun move(bottom: Int) {
        mTop = bottom - height
        this.top = mTop
        this.bottom = bottom
    }

    private fun tryBind() {
        val composition = composition ?: return
        val item = item ?: return
        if (!isVisible) isVisible = true
        binder.onBind(item)
        binder.bindComposition(composition)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        if (paddingTop > 0 && insetColor != 0 && top < 0 && top > -height) {
            paint.style = Paint.Style.FILL
            paint.color = insetColor
            val top = -top.toFloat()
            canvas.drawRect(0f, top, width.toFloat(), top + paddingTop.toFloat(), paint)
        }
    }
}