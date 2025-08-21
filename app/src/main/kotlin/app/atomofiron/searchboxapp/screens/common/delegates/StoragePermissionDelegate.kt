package app.atomofiron.searchboxapp.screens.common.delegates

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Environment
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import app.atomofiron.common.arch.Registerable
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.permission.PermissionDelegate
import app.atomofiron.common.util.property.WeakProperty
import app.atomofiron.searchboxapp.android.Intents

class StoragePermissionDelegate(
    private val fragment: WeakProperty<out Fragment>,
) : ActivityResultCallback<ActivityResult>, Registerable {

    private val permissions = PermissionDelegate.create(fragment.map { it?.activity })
    private var contractLauncher: ActivityResultLauncher<Intent>? = null
    private var callback: (() -> Unit)? = null

    override fun register() {
        fragment {
            permissions.register(this)
        }
        contractLauncher = fragment {
            registerForActivityResult(ActivityResultContracts.StartActivityForResult(), this@StoragePermissionDelegate)
        }
    }

    override fun onActivityResult(result: ActivityResult) {
        val granted = when {
            Android.Below.R -> fragment { requireContext().checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED }
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
            ?: fragment { startActivity(intent) }
    }
}