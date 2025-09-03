package app.atomofiron.searchboxapp.screens.curtain.model

import android.os.Bundle
import app.atomofiron.common.arch.BaseRouter

class CurtainPresenterParams(
    val recipient: Int,
    val layoutId: Int,
) {
    companion object {
        private const val LAYOUT_ID = "LAYOUT_ID"

        fun args(recipient: Int, layoutId: Int): Bundle = Bundle().apply {
            putInt(BaseRouter.RECIPIENT, recipient)
            putInt(LAYOUT_ID, layoutId)
        }

        fun params(arguments: Bundle) = CurtainPresenterParams(
            recipient = arguments.getInt(BaseRouter.RECIPIENT),
            layoutId = arguments.getInt(LAYOUT_ID),
        )
    }
}