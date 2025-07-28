package app.atomofiron.searchboxapp.screens.finder.adapter.holder

import android.graphics.Paint
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ItemTextFieldBinding
import kotlin.math.max

class TextFieldHolderDelegate(
    private val binding: ItemTextFieldBinding,
) {
    private val resources = binding.root.resources
    private val commonMargin = binding.box.run { marginStart + marginEnd }
    private val hintPadding = commonMargin + 2 * resources.getDimension(R.dimen.collapsed_hint_margin)
    private val textPadding = commonMargin + binding.field.run { paddingStart + paddingEnd }
    private val hintPaint = Paint()
    private var minWidth = 0f

    init {
        // TextAppearance.Material3.BodySmall
        hintPaint.textSize = binding.root.resources.getDimension(R.dimen.collapsed_hint)
        binding.field.run { imeOptions = imeOptions or EditorInfo.IME_ACTION_DONE }
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

    fun minWidth(): Float = binding.run {
        minWidth = max(
            hintPadding + hintPaint.measureText(box.hint.toString()),
            textPadding + field.paint.measureText(field.text.toString()),
        )
        return minWidth
    }
}
