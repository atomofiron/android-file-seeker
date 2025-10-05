package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.searchboxapp.utils.Const

data class NodeRef(val path: NodePath) {
    val uniqueId: Int = path.uniqueId
    val name: String = path.name
    val isContent = path.string.startsWith(Const.SCHEME_CONTENT)

    constructor(path: String) : this(NodePath(path))
}
