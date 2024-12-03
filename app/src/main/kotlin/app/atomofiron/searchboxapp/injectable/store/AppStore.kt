package app.atomofiron.searchboxapp.injectable.store

import android.content.Context
import android.content.res.Resources
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import app.atomofiron.common.util.property.MutableStrongProperty
import app.atomofiron.common.util.property.MutableWeakProperty
import kotlinx.coroutines.CoroutineScope

class AppStore constructor(
    val context: Context,
    val scope: CoroutineScope,
    resources: Resources,
) {
    val activityProperty = MutableWeakProperty<AppCompatActivity>(null)
    val windowProperty = MutableWeakProperty<Window>(null)
    val insetsControllerProperty = MutableStrongProperty<WindowInsetsControllerCompat?>()
    val resourcesProperty = MutableStrongProperty(resources)

    fun onActivityCreate(activity: AppCompatActivity) {
        activityProperty.value = activity
        windowProperty.value = activity.window
        insetsControllerProperty.value = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
    }

    fun onResourcesChange(resources: Resources) {
        resourcesProperty.value = resources
    }

    fun onActivityDestroy() {
        insetsControllerProperty.value = null
    }
}