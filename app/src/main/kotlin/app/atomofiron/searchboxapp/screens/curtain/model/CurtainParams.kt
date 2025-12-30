package app.atomofiron.searchboxapp.screens.curtain.model

import android.os.Bundle
import app.atomofiron.common.arch.BaseRouter

class CurtainParams(
    val id: CurtainId,
    val recipient: Int,
) {
    companion object {
        private const val ID = "ID"

        fun args(key: CurtainKey, recipient: Int) = Bundle().apply {
            putString(ID, key.id)
            putInt(BaseRouter.RECIPIENT, recipient)
        }

        fun params(arguments: Bundle) = CurtainParams(
            id = arguments.getString(ID, ""),
            recipient = arguments.getInt(BaseRouter.RECIPIENT),
        )
    }
}