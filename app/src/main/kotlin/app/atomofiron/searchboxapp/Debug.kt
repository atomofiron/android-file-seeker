package app.atomofiron.searchboxapp

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.view.View.MeasureSpec
import app.atomofiron.fileseeker.BuildConfig
import kotlinx.coroutines.delay

private var timestamp: Long = 0
private var nanotimestamp: Long = 0

private const val mute = false
private const val delay = false

fun Any.sleep(t: Long) = if (delay) Thread.sleep(t) else Unit

fun Any.logE(s: String) {
    if (!BuildConfig.DEBUG) {
        // reportError(s, null)
    }
    Log.e("searchboxapp", "[ERROR] [${this.javaClass.simpleName}] $s")
}

suspend fun debugDelay(seconds: Int = 1) = if (BuildConfig.DEBUG) delay(seconds * 1000L) else Unit

fun Any.poop(s: String) = poop(this.javaClass.simpleName, s)

fun Any.poop(context: Any, s: String) = poop(context.javaClass.simpleName, s)

@SuppressLint("ResourceType")
fun View.info() = "${this::class.java.simpleName}(${if (id <= 1) id.toString() else resources.getResourceEntryName(id)})"

fun Any.poop(label: String, s: String) {
    Log.e("searchboxapp", "[$label] $s")
}

fun Any.logI(s: String) {
    if (mute) return
    Log.i("searchboxapp", "[${this.javaClass.simpleName}] $s")
}

fun Any.logD(s: String) {
    if (mute) return
    Log.d("searchboxapp", "[${this.javaClass.simpleName}] $s")
}

fun Any.tik(s: String) {
    if (mute) return

    val now = System.currentTimeMillis()
    val dif = now - timestamp
    timestamp = now
    Log.e("searchboxapp", "[${this.javaClass.simpleName}] $dif $s")
}

fun Any.natik(s: String) {
    if (mute) return

    val now = System.nanoTime()
    val dif = now - nanotimestamp
    nanotimestamp = now
    Log.e("searchboxapp", "[${this.javaClass.simpleName}] $dif $s")
}

val Any?.simpleName: String get() = this?.javaClass?.simpleName.toString()

val Any?.className: String get() = this?.javaClass?.name.toString()

fun Int.size(): Int = MeasureSpec.getSize(this)

fun Int.mode(): String = when (MeasureSpec.getMode(this)) {
    MeasureSpec.AT_MOST -> "AT_MOST"
    MeasureSpec.EXACTLY -> "EXACTLY"
    MeasureSpec.UNSPECIFIED -> "UNSPECIFIED"
    else -> toString()
}
