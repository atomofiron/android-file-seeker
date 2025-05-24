package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.searchboxapp.model.explorer.Node.Companion.toUniqueId
import app.atomofiron.searchboxapp.utils.Const

data class NodeRef(
    val path: String,
    val uniqueId: Int = path.toUniqueId(),
) {
    val isContent = path.startsWith(Const.SCHEME_CONTENT)
}
