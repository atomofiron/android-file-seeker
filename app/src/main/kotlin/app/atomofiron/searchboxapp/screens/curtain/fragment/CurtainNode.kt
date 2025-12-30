package app.atomofiron.searchboxapp.screens.curtain.fragment

import android.view.View
import android.view.ViewGroup
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainId

class CurtainNode(
    val curtainId: CurtainId,
    var view: View?,
    var isCancelable: Boolean,
) {
    fun removeParent() {
        val parent = view?.parent as? ViewGroup
        parent?.removeView(view)
    }
}