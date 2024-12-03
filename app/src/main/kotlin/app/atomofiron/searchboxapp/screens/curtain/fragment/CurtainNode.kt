package app.atomofiron.searchboxapp.screens.curtain.fragment

import android.view.View
import android.view.ViewGroup

class CurtainNode(
    val layoutId: Int,
    var view: View?,
    var isCancelable: Boolean,
) {
    fun removeParent() {
        val parent = view?.parent as? ViewGroup
        parent?.removeView(view)
    }
}