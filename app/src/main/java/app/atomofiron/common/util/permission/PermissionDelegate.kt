package app.atomofiron.common.util.permission

import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import app.atomofiron.common.util.property.WeakProperty

class PermissionDelegate private constructor(
    activityProperty: WeakProperty<out FragmentActivity>,
) : PermissionDelegateApi {
    companion object {
        private val contract = ActivityResultContracts.RequestMultiplePermissions()

        fun create(activityProperty: WeakProperty<out FragmentActivity>): PermissionDelegateApi {
            return PermissionDelegate(activityProperty)
        }
    }

    private sealed class Status(val permission: String) {
        class Granted(permission: String) : Status(permission)
        class Denied(permission: String, val shouldShowRequestPermissionRationale: Boolean) : Status(permission)
    }

    private val activity by activityProperty

    private val requestedPermissions = mutableListOf<String>()
    private val permissionStatuses = mutableListOf<Status>()

    private val grantedCallbacks = mutableMapOf<String, List<PermissionCallback>>()
    private val deniedCallbacks = mutableMapOf<String, List<PermissionCallback>>()

    private var contractLauncher: ActivityResultLauncher<Array<String>>? = null

    override fun registerForActivityResult(fragment: Fragment) {
        contractLauncher = fragment.registerForActivityResult(contract, this)
    }

    override fun check(vararg permissions: String): PermissionDelegateApi {
        val notGranted = activity?.filterNotGranted(*permissions)
        notGranted?.forEach { permission ->
            activity?.run {
                val should = shouldShowRequestPermissionRationale(permission)
                notifyDenied(permission, should)
            }
        }
        return this
    }

    override fun request(vararg permissions: String): PermissionDelegateApi {
        val contract = contractLauncher ?: return this
        val notGranted = activity?.filterNotGranted(*permissions)
        requestedPermissions.clear()
        requestedPermissions.addAll(permissions)
        if (!notGranted.isNullOrEmpty()) {
            contract.launch(notGranted.toTypedArray())
        }
        return this
    }

    override fun onActivityResult(result: Map<String, Boolean>) {
        requestedPermissions.removeAll(result.keys)
        result.entries.forEach { entry ->
            when {
                entry.value -> {
                    rememberStatus(Status.Granted(entry.key))
                    notifyGranted(entry.key)
                }
                else -> {
                    val should = activity?.shouldShowRequestPermissionRationale(entry.key) != false
                    rememberStatus(Status.Denied(entry.key, should))
                    notifyDenied(entry.key, should)
                }
            }
        }
    }

    private fun FragmentActivity.filterNotGranted(vararg permissions: String): List<String> {
        val notGranted = mutableListOf<String>()
        for (permission in permissions) {
            when {
                isGranted(permission) -> Unit
                checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED -> rememberStatus(Status.Granted(permission))
                else -> notGranted.add(permission)
            }
        }
        return notGranted
    }

    override fun granted(callback: GrantedCallback): PermissionDelegateApi {
        requestedPermissions.forEach { permission ->
            when {
                isGranted(permission) -> callback(permission)
                requestedPermissions.contains(permission) -> grantedCallbacks.add(permission, callback)
            }
        }
        return this
    }

    override fun denied(callback: DeniedCallback): PermissionDelegateApi {
        requestedPermissions.forEach { permission ->
            when {
                isGranted(permission) -> Unit
                requestedPermissions.contains(permission) -> deniedCallbacks.add(permission, callback)
                else -> when (val status = getStatus(permission)) {
                    is Status.Denied -> callback(permission, status.shouldShowRequestPermissionRationale)
                    else -> Unit
                }
            }
        }
        return this
    }

    override fun any(callback: ExactAnyCallback): PermissionDelegateApi {
        granted { callback() }
        denied { _, _ -> callback() }
        return this
    }

    override fun granted(permission: String, callback: ExactGrantedCallback): PermissionDelegateApi {
        when {
            isGranted(permission) -> callback()
            requestedPermissions.contains(permission) -> grantedCallbacks.add(permission, callback)
        }
        return this
    }

    override fun denied(permission: String, callback: ExactDeniedCallback): PermissionDelegateApi {
        when {
            isGranted(permission) -> Unit
            requestedPermissions.contains(permission) -> deniedCallbacks.add(permission, callback)
            else -> when (val status = getStatus(permission)) {
                is Status.Denied -> callback(status.shouldShowRequestPermissionRationale)
                else -> Unit
            }
        }
        return this
    }

    private fun getStatus(permission: String): Status? = permissionStatuses.find { it.permission == permission }

    private fun isGranted(permission: String): Boolean = getStatus(permission) is Status.Granted

    private fun rememberStatus(status: Status) {
        val index = permissionStatuses.indexOfFirst { it.permission == status.permission }
        when {
            index > 0 -> permissionStatuses[index] = status
            else -> permissionStatuses.add(status)
        }
    }

    private fun notifyGranted(permission: String) {
        grantedCallbacks[permission]?.forEach { callback ->
            when (callback) {
                is GrantedCallback -> callback(permission)
                is ExactGrantedCallback -> callback()
            }
        }
        grantedCallbacks.remove(permission)
        deniedCallbacks.remove(permission)
    }

    private fun notifyDenied(permission: String, shouldShowRequestPermissionRationale: Boolean) {
        deniedCallbacks[permission]?.forEach { callback ->
            when (callback) {
                is DeniedCallback -> callback(permission, shouldShowRequestPermissionRationale)
                is ExactDeniedCallback -> callback(shouldShowRequestPermissionRationale)
            }
        }
        grantedCallbacks.remove(permission)
        deniedCallbacks.remove(permission)
    }

    private fun MutableMap<String, List<PermissionCallback>>.add(permission: String, callback: PermissionCallback) {
        val callbacks = get(permission)?.toMutableList() ?: mutableListOf()
        callbacks.add(callback)
        put(permission, callbacks)
    }
}