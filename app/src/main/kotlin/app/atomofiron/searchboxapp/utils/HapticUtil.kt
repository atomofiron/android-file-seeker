package app.atomofiron.searchboxapp.utils

import android.view.HapticFeedbackConstants
import android.view.View

fun View.setHapticEffect(enabled: Boolean) {
    rootView.isHapticFeedbackEnabled = enabled
}

fun View.performHapticEffect(effect: Int) {
    rootView.performHapticFeedback(effect)
}

fun View.performHapticLite() = performHapticEffect(HapticFeedbackConstants.KEYBOARD_TAP)

fun View.performHapticHeavy() = performHapticEffect(HapticFeedbackConstants.CLOCK_TICK)
