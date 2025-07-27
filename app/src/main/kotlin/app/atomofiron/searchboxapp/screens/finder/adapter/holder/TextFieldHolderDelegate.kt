package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.graphics.Paint
import android.view.Gravity
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemTextFieldBinding

class TextFieldHolderDelegate(
    private val binding: ItemTextFieldBinding,
) {
    private val hintMargin = binding.root.run {
        marginStart + marginEnd + resources.getDimension(R.dimen.collapsed_hint_margin) * 2
    }
    private val hintPaint = Paint()
    private var minWidth = 0f

    init {
        // TextAppearance.Material3.BodySmall
        hintPaint.textSize = binding.root.resources.getDimension(R.dimen.collapsed_hint)
        binding.root.addOnLayoutChangeListener { view, _, _, _, _, _, _, _, _ ->
            when {
                view.width > minWidth -> Gravity.START
                else -> Gravity.CENTER_HORIZONTAL
            }.also {
                binding.box.gravity = it
                binding.field.gravity = it
            }
        }
    }

    fun minWidth(): Float {
        minWidth = hintMargin + hintPaint.measureText(binding.box.hint.toString())
        return minWidth
    }
}
