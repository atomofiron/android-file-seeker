package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.searchboxapp.utils.Const

data class NodeRef(
    val path: String,
    val uniqueId: Int = 0,
) {
    val isContent = path.startsWith(Const.SCHEME_CONTENT)

    constructor(path: NodePath) : this(path.string, path.uniqueId)
}
