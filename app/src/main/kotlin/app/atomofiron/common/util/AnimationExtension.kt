package app.atomofiron.common.util

import android.content.Context
import android.provider.Settings
import android.view.animation.Animation
import android.view.animation.AnimationUtils

fun Context.loadAnimationWithDurationScale(animRes: Int): Animation {
    return AnimationUtils.loadAnimation(this, animRes).apply {
        duration = calculateDurationWithScale(duration)
    }
}

fun Context.calculateDurationWithScale(duration: Long): Long {
    return (duration.toFloat() * getAnimatorDurationScale()).toLong()
}

fun Context.getAnimatorDurationScale(): Float {
    return try {
        Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
    } catch (exc: Settings.SettingNotFoundException) {
        return 1f
    }
}
