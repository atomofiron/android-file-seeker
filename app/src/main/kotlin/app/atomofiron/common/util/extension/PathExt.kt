package app.atomofiron.common.util.extension

import android.graphics.Path
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

fun Path.corner(x: Float, y: Float, left: Boolean, top: Boolean, clockWise: Boolean, radius: Float, offsetX: Float = 0f, offsetY: Float = 0f) {
    var count = if (left) 0 else 1
    count += if (top) 0 else 1
    count += if (clockWise) 1 else 0
    val x0 = x + when (count) {
        0, 2 -> radius * dX(top, clockWise)
        else -> 0f
    }
    val y0 = y + when (count) {
        1, 3 -> radius * dY(left, clockWise)
        else -> 0f
    }
    val x1 = x + when (count) {
        1, 3 -> radius * dX(!top, clockWise)
        else -> 0f
    }
    val y1 = y + when (count) {
        0, 2 -> radius * dY(!left, clockWise)
        else -> 0f
    }
    corner(x0, y0, x1, y1, clockWise, offsetX, offsetY)
}

fun Path.corner(x0: Float, y0: Float, x1: Float, y1: Float, clockWise: Boolean, offsetX: Float = 0f, offsetY: Float = 0f) {
    var dx = x1 - x0
    var dy = y1 - y0
    if (abs(dx) != abs(dy)) {
        throw IllegalArgumentException("($x0,$y0) - ($x1,$y1)")
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
    val left = min(min(min(x0, x1), x2), x3)
    val top = min(min(min(y0, y1), y2), y3)
    val right = max(max(max(x0, x1), x2), x3)
    val bottom = max(max(max(y0, y1), y2), y3)
    arcTo(left + offsetX, top + offsetY, right + offsetX, bottom + offsetY, start, sweep, false)
}

private fun dX(dx: Float, dy: Float, clockWise: Boolean): Float = dx * dX(dx.sign == dy.sign, clockWise)

private fun dY(dx: Float, dy: Float, clockWise: Boolean): Float = dy * dY(dx.sign == dy.sign, clockWise)

private fun dX(magicalFlag: Boolean, clockWise: Boolean) = if (magicalFlag == clockWise) -1 else 1

private fun dY(magicalFlag: Boolean, clockWise: Boolean) = if (magicalFlag == clockWise) 1 else -1
