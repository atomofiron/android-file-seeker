package app.atomofiron.searchboxapp

import android.util.Log

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

fun Any.poop(s: String) = poop(this.javaClass.simpleName, s)

fun Any.poop(context: Any, s: String) = poop(context.javaClass.simpleName, s)

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
