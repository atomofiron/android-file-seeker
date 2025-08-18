package app.atomofiron.searchboxapp.screens.finder.fragment.keyboard

import android.view.animation.Interpolator
import kotlin.math.exp

/** Controls the viscous fluid effect (how much of it).  */
private const val VISCOUS_FLUID_SCALE = 8.0f

// must be set to 1.0 (used in viscousFluid())
private val VISCOUS_FLUID_NORMALIZE = 1.0f / viscousFluid(1.0f)
// account for very small floating-point error
private val VISCOUS_FLUID_OFFSET = 1.0f - VISCOUS_FLUID_NORMALIZE * viscousFluid(1.0f)

// android.widget.Scroller.ViscousFluidInterpolator
class ViscousFluidInterpolator : Interpolator {
    override fun getInterpolation(input: Float): Float {
        val interpolated = VISCOUS_FLUID_NORMALIZE * viscousFluid(input)
        return when {
            interpolated > 0 -> interpolated + VISCOUS_FLUID_OFFSET
            else -> interpolated
        }
    }
}

private fun viscousFluid(x: Float): Float {
    var scaled = x * VISCOUS_FLUID_SCALE
    if (scaled < 1.0f) {
        scaled -= (1.0f - exp(-scaled.toDouble()).toFloat())
    } else {
        val start = 0.36787945f // 1/e == exp(-1)
        scaled = 1.0f - exp((1.0f - scaled).toDouble()).toFloat()
        scaled = start + scaled * (1.0f - start)
    }
    return scaled
}