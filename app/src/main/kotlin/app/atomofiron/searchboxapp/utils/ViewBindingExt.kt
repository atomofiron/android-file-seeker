package app.atomofiron.searchboxapp.utils

import android.content.Context
import android.content.res.Resources
import android.view.View
import androidx.viewbinding.ViewBinding

val ViewBinding.context: Context get() = root.context

val ViewBinding.resources: Resources get() = root.resources

inline fun <B : ViewBinding> B.root(action: View.() -> Unit) = root.action()
