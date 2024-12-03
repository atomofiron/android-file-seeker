package app.atomofiron.searchboxapp.screens.curtain.model

import android.os.Bundle
import app.atomofiron.common.arch.BaseRouter

class CurtainPresenterParams(
    val recipient: String,
    val layoutId: Int,
) {
    companion object {
        private const val LAYOUT_ID = "LAYOUT_ID"

        fun args(recipient: String, layoutId: Int): Bundle = Bundle().apply {
            putString(BaseRouter.RECIPIENT, recipient)
            putInt(LAYOUT_ID, layoutId)
        }

        fun params(arguments: Bundle) = CurtainPresenterParams(
            recipient = arguments.getString(BaseRouter.RECIPIENT)!!,
            layoutId = arguments.getInt(LAYOUT_ID),
        )
    }
}