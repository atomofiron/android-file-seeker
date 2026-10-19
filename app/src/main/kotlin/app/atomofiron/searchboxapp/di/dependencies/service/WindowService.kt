package app.atomofiron.searchboxapp.di.dependencies.service

import android.view.Window
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import app.atomofiron.common.util.Android
import app.atomofiron.fileseeker.BuildConfig
import app.atomofiron.searchboxapp.screens.main.MainScope
import app.atomofiron.searchboxapp.screens.main.di.AppStore
import javax.inject.Inject

@MainScope
class WindowService @Inject constructor(
    appStore: AppStore,
) {
    private val window by appStore.windowProperty
    private val controller by appStore.insetsControllerProperty

    fun setLightStatusBar(value: Boolean) {
        controller?.isAppearanceLightStatusBars = value
    }

    fun setLightNavigationBar(value: Boolean) {
        controller?.isAppearanceLightNavigationBars = value
    }

    fun setSecureFlag(value: Boolean) = window.apply {
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
        Android.R -> insetsController?.systemBarsAppearance ?: 0
        else -> 0
    }

    private fun Window.setSystemBarsAppearance(flags: Int) = when {
        Android.R -> insetsController?.setSystemBarsAppearance(flags, flags)
        else -> Unit
    }
}