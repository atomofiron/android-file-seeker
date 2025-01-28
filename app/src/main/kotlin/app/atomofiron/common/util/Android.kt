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

}