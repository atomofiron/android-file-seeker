package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Constraints
import androidx.coordinatorlayout.widget.CoordinatorLayout
import app.atomofiron.searchboxapp.className
import app.atomofiron.searchboxapp.utils.ExtType
import lib.atomofiron.insets.insetsPadding

class SnackbarContainerLayout : CoordinatorLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        insetsPadding(ExtType { barsWithCutout + joystickBottom + joystickFlank + navigation + rail })
        if (isInEditMode) {
            layoutParams = FrameLayout.LayoutParams(0, 0)
        }
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        when (params) {
            null -> Unit
            is LayoutParams -> params.gravity = Gravity.BOTTOM
            is FrameLayout.LayoutParams -> params.gravity = Gravity.BOTTOM
            is ConstraintLayout.LayoutParams -> {
                params.bottomToBottom = Constraints.LayoutParams.PARENT_ID
            }
            else -> throw IllegalArgumentException(params::class.className)
        }
        when (params) {
            is LayoutParams,
            is FrameLayout.LayoutParams -> {
                params.width = LayoutParams.MATCH_PARENT
                params.height = LayoutParams.WRAP_CONTENT
            }
        }
        super.setLayoutParams(params)
    }
}