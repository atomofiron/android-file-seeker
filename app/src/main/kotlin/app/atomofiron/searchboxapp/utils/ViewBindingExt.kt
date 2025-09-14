package app.atomofiron.searchboxapp.utils

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

val ViewBinding.context: Context get() = root.context

val ViewBinding.resources: Resources get() = root.resources

inline fun <B : ViewBinding> B.root(action: View.() -> Unit) = root.action()

inline fun <B : ViewBinding> ViewGroup.create(action: (LayoutInflater, ViewGroup, Boolean) -> B): B {
    return action(LayoutInflater.from(context), this, false)
}

inline fun <B : ViewBinding> ViewGroup.attach(action: (LayoutInflater, ViewGroup, Boolean) -> B): B {
    return action(LayoutInflater.from(context), this, true)
}

inline fun <B : ViewBinding> ViewGroup.attach(action: (LayoutInflater, ViewGroup) -> B): B {
    return action(LayoutInflater.from(context), this)
}
