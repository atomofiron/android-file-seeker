package app.atomofiron.searchboxapp.injectable.service

import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.R
import android.view.Window
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import app.atomofiron.searchboxapp.BuildConfig
import app.atomofiron.searchboxapp.injectable.store.AppStore

class WindowService(
    appStore: AppStore,
) {
    private val window by appStore.windowProperty
    private val controller by appStore.insetsControllerProperty

    var isLightStatusBars: Boolean = false
        get() = controller?.isAppearanceLightStatusBars ?: field
        set(value) {
            field = value
            controller?.isAppearanceLightStatusBars = value
        }

    var isLightNavigationBars: Boolean = false
        get() = controller?.isAppearanceLightNavigationBars ?: field
        set(value) {
            field = value
            controller?.isAppearanceLightNavigationBars = value
        }

    fun setSecureFlag(value: Boolean) = window?.apply {
        val appearance = getSystemBarsAppearance()
        when {
            BuildConfig.DEBUG -> Unit
            value -> addFlags(FLAG_SECURE)
            else -> clearFlags(FLAG_SECURE)
        }
        setSystemBarsAppearance(appearance)
    }

    // эти ёбаные флаги тупо слетают
    private fun Window.getSystemBarsAppearance(): Int = when {
        SDK_INT >= R -> insetsController?.systemBarsAppearance ?: 0
        else -> 0
    }

    private fun Window.setSystemBarsAppearance(flags: Int) = when {
        SDK_INT >= R -> insetsController?.setSystemBarsAppearance(flags, flags)
        else -> Unit
    }
}