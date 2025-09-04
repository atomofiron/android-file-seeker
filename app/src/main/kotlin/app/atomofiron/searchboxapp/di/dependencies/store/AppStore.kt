package app.atomofiron.searchboxapp.di.dependencies.store

import android.content.res.Resources
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import app.atomofiron.common.util.property.MutableStrongProperty
import app.atomofiron.common.util.property.StrongProperty

// todo soo then all deps should be provided as impls of interfaces (because of SomeDep : AppStore by appStore)

interface AppStore {
    val activity: AppCompatActivity?
    val activityProperty: StrongProperty<AppCompatActivity>
    val windowProperty: StrongProperty<Window>
    val insetsControllerProperty: StrongProperty<WindowInsetsControllerCompat?>
}

interface AppStoreConsumer {
    fun onActivityCreate(activity: AppCompatActivity)
    fun onResourcesChange(resources: Resources)
    fun onActivityDestroy()
}

interface AppStoreProvider {
    val appStore: AppStore
}

class AndroidStore(activity: AppCompatActivity) : AppStore, AppStoreConsumer {

    override val activityProperty = MutableStrongProperty(activity)
    override val windowProperty = MutableStrongProperty<Window>(activity.window)
    override val insetsControllerProperty = MutableStrongProperty<WindowInsetsControllerCompat?>(null)

    override val activity by activityProperty

    override fun onActivityCreate(activity: AppCompatActivity) {
        activityProperty.value = activity
        windowProperty.value = activity.window
        insetsControllerProperty.value = WindowInsetsControllerCompat(activity.window, activity.window.decorView)
    }

    override fun onResourcesChange(resources: Resources) {
    }

    override fun onActivityDestroy() {
        insetsControllerProperty.value = null
    }
}