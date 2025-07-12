package app.atomofiron.searchboxapp.screens.delegates

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.permission.PermissionDelegate
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.android.Intents

class StoragePermissionDelegate(
    private val activity: WeakProperty<out ComponentActivity>,
) : ActivityResultCallback<ActivityResult> {

    private val permissions = PermissionDelegate.create(activity)
    private var contractLauncher: ActivityResultLauncher<Intent>? = null
    private var callback: (() -> Unit)? = null

    init {
        activity.observe { // leaks?
            permissions.register(it ?: return@observe)
        }
        contractLauncher = activity {
            registerForActivityResult(ActivityResultContracts.StartActivityForResult(), this@StoragePermissionDelegate)
        }
    }

    override fun onActivityResult(result: ActivityResult) {
        val granted = when {
            Android.Below.R -> activity { checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED }
            else -> Environment.isExternalStorageManager()
        }
        callback?.takeIf { granted == true }?.invoke()
    }

    fun request(granted: () -> Unit, denied: (shouldShowRationale: Boolean) -> Unit) {
        when {
            Android.Below.R -> permissions.request(WRITE_EXTERNAL_STORAGE)
                .granted { granted() }
                .denied { _, should -> denied(should) }
            Environment.isExternalStorageManager() -> granted()
            else -> launchSettings(granted)
        }
    }

    fun launchSettings(callback: (() -> Unit)? = null) {
        this.callback = callback ?: this.callback
        val intent = when {
            Android.R -> Intents.storagePermissionIntent
            else -> Intents.settingsIntent
        }
        contractLauncher?.launch(intent)
            ?: activity { startActivity(intent) }
    }
}