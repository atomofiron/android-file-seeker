package app.atomofiron.searchboxapp.injectable.store

import android.content.Context
import android.content.res.Resources
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import app.atomofiron.common.util.property.MutableStrongProperty
import app.atomofiron.common.util.property.MutableWeakProperty
import app.atomofiron.common.util.property.StrongProperty
import app.atomofiron.common.util.property.WeakProperty
import kotlinx.coroutines.CoroutineScope

// todo soo then all deps should be provided as impls of interfaces (because of SomeDep : AppStore by appStore)

interface AppStore {
    val context: Context
    val resources: Resources
    val appScope: CoroutineScope
    val activity: AppCompatActivity?
    val resourcesProperty: StrongProperty<Resources>
    val activityProperty: WeakProperty<AppCompatActivity>
    val windowProperty: WeakProperty<Window>
    val insetsControllerProperty: StrongProperty<WindowInsetsControllerCompat?>
}

interface AppStoreConsumer {
    fun onActivityCreate(activity: AppCompatActivity)
    fun onResourcesChange(resources: Resources)
    fun onActivityDestroy()
}

class AndroidStore(
    override val context: Context,
    override val appScope: CoroutineScope,
    override val resourcesProperty: AppResources,
) : AppStore, AppStoreConsumer {

    override val activityProperty = MutableWeakProperty<AppCompatActivity>()
    override val windowProperty = MutableWeakProperty<Window>()
    override val insetsControllerProperty = MutableStrongProperty<WindowInsetsControllerCompat?>(null)

    override val activity by activityProperty
    override val resources by resourcesProperty

    override fun onActivityCreate(activity: AppCompatActivity) {
        activityProperty.value = activity
        windowProperty.value = activity.window
        insetsControllerProperty.value = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
    }

    override fun onResourcesChange(resources: Resources) {
        resourcesProperty.value = resources
    }

    override fun onActivityDestroy() {
        insetsControllerProperty.value = null
    }
}