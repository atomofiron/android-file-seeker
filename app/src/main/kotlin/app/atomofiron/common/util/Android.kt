package app.atomofiron.common.util

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES as Sdk
import androidx.annotation.ChecksSdkIntAtLeast

object Android {

    object Below {
        val R get() = SDK_INT < Sdk.R
        val T get() = SDK_INT < Sdk.TIRAMISU
    }

    val N_MR1 @ChecksSdkIntAtLeast(api = Sdk.N_MR1)
        get() = SDK_INT >= Sdk.N_MR1
    val O @ChecksSdkIntAtLeast(api = Sdk.O)
        get() = SDK_INT >= Sdk.O
    val O_MR1 @ChecksSdkIntAtLeast(api = Sdk.O_MR1)
        get() = SDK_INT >= Sdk.O_MR1
    val P @ChecksSdkIntAtLeast(api = Sdk.P)
        get() = SDK_INT >= Sdk.P
    val Q @ChecksSdkIntAtLeast(api = Sdk.Q)
        get() = SDK_INT >= Sdk.Q
    val R @ChecksSdkIntAtLeast(api = Sdk.R)
        get() = SDK_INT >= Sdk.R
    val S @ChecksSdkIntAtLeast(api = Sdk.S)
        get() = SDK_INT >= Sdk.S
    val S_V2 @ChecksSdkIntAtLeast(api = Sdk.S_V2)
        get() = SDK_INT >= Sdk.S_V2
    val T @ChecksSdkIntAtLeast(api = Sdk.TIRAMISU)
        get() = SDK_INT >= Sdk.TIRAMISU
    val U @ChecksSdkIntAtLeast(api = Sdk.UPSIDE_DOWN_CAKE)
        get() = SDK_INT >= Sdk.UPSIDE_DOWN_CAKE
    val V @ChecksSdkIntAtLeast(api = Sdk.VANILLA_ICE_CREAM)
        get() = SDK_INT >= Sdk.VANILLA_ICE_CREAM

    operator fun get(sdk: Int) = when (sdk) {
        Sdk.BASE -> "Android 1.0"
        Sdk.BASE_1_1 -> "Android 1.1"
        Sdk.CUPCAKE -> "Android 1.5"
        Sdk.DONUT -> "Android 1.6"
        Sdk.ECLAIR -> "Android 2.0"
        Sdk.ECLAIR_0_1 -> "Android 2.0.1"
        Sdk.ECLAIR_MR1 -> "Android 2.1"
        Sdk.FROYO -> "Android 2.2"
        Sdk.GINGERBREAD -> "Android 2.3"
        Sdk.GINGERBREAD_MR1 -> "Android 2.3.3"
        Sdk.HONEYCOMB -> "Android 3.0"
        Sdk.HONEYCOMB_MR1 -> "Android 3.1"
        Sdk.HONEYCOMB_MR2 -> "Android 3.2"
        Sdk.ICE_CREAM_SANDWICH -> "Android 4"
        Sdk.ICE_CREAM_SANDWICH_MR1 -> "Android 4.0.3"
        Sdk.JELLY_BEAN -> "Android 4.1"
        Sdk.JELLY_BEAN_MR1 -> "Android 4.2"
        Sdk.JELLY_BEAN_MR2 -> "Android 4.3"
        Sdk.KITKAT -> "Android 4.4"
        Sdk.KITKAT_WATCH -> "Android 4.4W"
        Sdk.LOLLIPOP -> "Android 5.0"
        Sdk.LOLLIPOP_MR1 -> "Android 5.1"
        Sdk.M -> "Android 6.0"
        Sdk.N -> "Android 7.0"
        Sdk.N_MR1 -> "Android 7.1"
        Sdk.O -> "Android 8.0"
        Sdk.O_MR1 -> "Android 8.1"
        Sdk.P -> "Android 9"
        Sdk.Q -> "Android 10"
        Sdk.R -> "Android 11"
        Sdk.S -> "Android 12"
        Sdk.S_V2 -> "Android 12L"
        Sdk.TIRAMISU -> "Android 13"
        Sdk.UPSIDE_DOWN_CAKE -> "Android 14"
        Sdk.VANILLA_ICE_CREAM -> "Android 15"
        36 -> "Android 16"
        else -> null
    }
}