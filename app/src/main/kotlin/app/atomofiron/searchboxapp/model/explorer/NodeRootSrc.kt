package app.atomofiron.searchboxapp.model.explorer

sealed interface NodeRootSrc {

    val ref: NodeRef

    data class Ref(override val ref: NodeRef) : NodeRootSrc

    data object Bluetooth : NodeRootSrc {
        override val ref = NodeRef("bluetooth")
    }

    data object Screenshots : NodeRootSrc {
        override val ref = NodeRef("screenshots")
    }

    companion object {
        operator fun invoke(ref: NodeRef): Ref = Ref(ref)
    }
}