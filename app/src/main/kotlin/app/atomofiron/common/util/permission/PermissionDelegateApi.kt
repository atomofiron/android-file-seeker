package app.atomofiron.common.util.permission

import androidx.activity.result.ActivityResultCallback
import androidx.fragment.app.Fragment

interface PermissionDelegateApi : ActivityResultCallback<Map<String, Boolean>> {
    fun registerForActivityResult(fragment: Fragment)
    fun check(vararg permissions: String): PermissionDelegateApi
    fun request(vararg permissions: String): PermissionDelegateApi
    fun granted(callback: GrantedCallback): PermissionDelegateApi
    fun denied(callback: DeniedCallback): PermissionDelegateApi
    fun any(callback: ExactAnyCallback): PermissionDelegateApi
    fun granted(permission: String, callback: ExactGrantedCallback): PermissionDelegateApi
    fun denied(permission: String, callback: ExactDeniedCallback): PermissionDelegateApi
}
