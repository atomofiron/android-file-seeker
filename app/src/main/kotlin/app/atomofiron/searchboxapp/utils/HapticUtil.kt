package app.atomofiron.searchboxapp.utils

import android.content.Context
import android.os.VibrationEffect.Composition.PRIMITIVE_CLICK
import android.os.VibrationEffect.Composition.PRIMITIVE_TICK
import android.os.VibrationEffect.EFFECT_CLICK
import android.os.VibrationEffect.EFFECT_HEAVY_CLICK
import android.os.VibrationEffect.EFFECT_TICK
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import app.atomofiron.common.util.Android

private const val LITE = HapticFeedbackConstants.KEYBOARD_TAP
private const val HEAVY = HapticFeedbackConstants.CLOCK_TICK

private var supported: Boolean? = null

fun Context.hasHaptic(): Boolean {
    return (supported ?: isHapticSupported()).also {
        supported = it
    }
}

@Suppress("DEPRECATION")
private fun Context.isHapticSupported(): Boolean {
    val vibrator = when {
        Android.S -> getSystemService(VibratorManager::class.java)?.defaultVibrator
        else -> getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    if (vibrator?.hasVibrator() != true) {
        return false
    }
    if (Android.S) vibrator.arePrimitivesSupported(PRIMITIVE_TICK, PRIMITIVE_CLICK)
        .any { it }
        .also { if (it) return true }
    if (Android.R) vibrator.areEffectsSupported(EFFECT_TICK, EFFECT_CLICK, EFFECT_HEAVY_CLICK)
        .any { it == Vibrator.VIBRATION_EFFECT_SUPPORT_YES }
        .also { if (it) return true }
    return Android.O && vibrator.hasAmplitudeControl()
}

fun View.performHapticEffect(effect: Int) {
    if (context.hasHaptic()) performHapticFeedback(effect)
}

fun View.performHapticLite() {
    if (context.hasHaptic()) performHapticFeedback(LITE)
}

fun View.performHapticHeavy() {
    if (context.hasHaptic()) performHapticFeedback(HEAVY)
}
