package app.atomofiron.common.util

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES

typealias AndroidSdk = VERSION_CODES

object Android {
    val N_MR1 get() = SDK_INT >= VERSION_CODES.N_MR1
    val O get() = SDK_INT >= VERSION_CODES.O
    val O_MR1 get() = SDK_INT >= VERSION_CODES.O_MR1
    val P get() = SDK_INT >= VERSION_CODES.P
    val Q get() = SDK_INT >= VERSION_CODES.Q
    val R get() = SDK_INT >= VERSION_CODES.R
    val S get() = SDK_INT >= VERSION_CODES.S
    val S_V2 get() = SDK_INT >= VERSION_CODES.S_V2
    val T get() = SDK_INT >= VERSION_CODES.TIRAMISU
    val U get() = SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE

    object Below {
        val R get() = SDK_INT < VERSION_CODES.R
    }
}