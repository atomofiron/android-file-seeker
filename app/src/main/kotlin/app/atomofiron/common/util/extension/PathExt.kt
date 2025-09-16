package app.atomofiron.common.util.extension

import android.graphics.Path
import android.util.DisplayMetrics
import app.atomofiron.common.util.extension.CornerPathDebug.density
import app.atomofiron.common.util.extension.CornerPathDebug.densityDpi
import app.atomofiron.common.util.extension.CornerPathDebug.heightPixels
import app.atomofiron.common.util.extension.CornerPathDebug.scaledDensity
import app.atomofiron.common.util.extension.CornerPathDebug.widthPixels
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign

object CornerPathDebug {
    var widthPixels = 0
    var heightPixels = 0
    var densityDpi = 0
    var density = 0f
    var scaledDensity = 0f

    operator fun invoke(metrics: DisplayMetrics) {
        widthPixels = metrics.widthPixels
        heightPixels = metrics.heightPixels
        densityDpi = metrics.densityDpi
        density = metrics.density
        scaledDensity = metrics.scaledDensity
    }
}

fun Path.corner(x: Float, y: Float, left: Boolean, top: Boolean, clockWise: Boolean, radius: Float, offsetX: Float = 0f, offsetY: Float = 0f) {
    val flag = (left == top) == clockWise
    val x0 = x + if (flag) 0.0 else (radius * dX(top, clockWise)).toDouble()
    val y0 = y + if (!flag) 0.0 else (radius * dY(left, clockWise)).toDouble()
    val x1 = x + if (!flag) 0.0 else (radius * dX(!top, clockWise)).toDouble()
    val y1 = y + if (flag) 0.0 else (radius * dY(!left, clockWise)).toDouble()
    corner(x0, y0, x1, y1, clockWise, offsetX, offsetY)
}

private fun Path.corner(x0: Double, y0: Double, x1: Double, y1: Double, clockWise: Boolean, offsetX: Float = 0f, offsetY: Float = 0f) {
    var dx = x1 - x0
    var dy = y1 - y0
    var accuracy = accuracyArr.size
    while (abs(dx) != abs(dy)) {
        if (--accuracy < 0) {
            throw IllegalArgumentException("($x0,$y0) - ($x1,$y1), ${abs(x1 - x0)} != ${abs(y1 - y0)}, ${widthPixels}x$heightPixels $densityDpi $density $scaledDensity")
        }
        dx = (x1 - x0).round(accuracy)
        dy = (y1 - y0).round(accuracy)
    }
    var start = when {
        dx < 0 && dy < 0 -> 0f // 90f
        dx > 0 && dy < 0 -> 90f // 180f
        dx > 0 && dy > 0 -> 180f // 270f
        else -> 270f // 360f
    }
    if (clockWise) start += 90f
    val sweep = if (clockWise) 90f else -90f
    val x2 = x1 + dX(dx, dy, clockWise)
    val y2 = y1 + dY(dx, dy, clockWise)
    dx = x2 - x1
    dy = y2 - y1
    val x3 = x2 + dX(dx, dy, clockWise)
    val y3 = y2 + dY(dx, dy, clockWise)
    val left = (min(min(min(x0, x1), x2), x3) + offsetX).toFloat()
    val top = (min(min(min(y0, y1), y2), y3) + offsetY).toFloat()
    val right = (max(max(max(x0, x1), x2), x3) + offsetX).toFloat()
    val bottom = (max(max(max(y0, y1), y2), y3) + offsetY).toFloat()
    arcTo(left, top, right, bottom, start, sweep, false)
}

private fun dX(dx: Double, dy: Double, clockWise: Boolean): Double = dx * dX(dx.sign == dy.sign, clockWise)

private fun dY(dx: Double, dy: Double, clockWise: Boolean): Double = dy * dY(dx.sign == dy.sign, clockWise)

private fun dX(magicalFlag: Boolean, clockWise: Boolean) = if (magicalFlag == clockWise) -1 else 1

private fun dY(magicalFlag: Boolean, clockWise: Boolean) = if (magicalFlag == clockWise) 1 else -1

private val accuracyArr = arrayOf(1, 10, 100, 1000, 10000, 100000, 1000000, 10000000)

private fun Double.round(accuracy: Int): Double = (this * accuracyArr[accuracy]).roundToInt() / accuracyArr[accuracy].toDouble()

fun nearby(distance: Float, x0: Float, y0: Float, x1: Float, y1: Float, x2: Float = x1, y2: Float = y1): Boolean {
    if (x0 == x1 && y0 == y1 || x0 == x2 && y0 == y2) {
        return true
    }
    return nearby(distance, x0, y0, x1, y1) || (x1 != x2 || y1 != y2) && nearby(distance, x0, y0, x2, y2)
}

private fun nearby(distance: Float, x0: Float, y0: Float, x1: Float, y1: Float): Boolean {
    val dx = abs(x1 - x0)
    val dy = abs(y1 - y0)
    return when {
        dx > distance && dy > distance -> false
        (distance / 2).let { dx < it && dy < it } -> true
        else -> dx.pow(2) + dy.pow(2) < distance.pow(2)
    }
}
