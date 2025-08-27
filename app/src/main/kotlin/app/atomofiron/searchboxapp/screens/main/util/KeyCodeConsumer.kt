package app.atomofiron.searchboxapp.screens.main.util

import androidx.fragment.app.Fragment

interface KeyCodeConsumer {
    fun onKeyDown(keyCode: Int): Boolean
}

fun Fragment.offerKeyCodeToChildren(keyCode: Int): Boolean {
    if (!isVisible) return false
    childFragmentManager.fragments.run {
        for (i in indices.reversed()) {
            if (get(i).offerKeyCodeToChildren(keyCode)) {
                return true
            }
        }
    }
    return when (this) {
        is KeyCodeConsumer -> onKeyDown(keyCode)
        else -> false
    }
}