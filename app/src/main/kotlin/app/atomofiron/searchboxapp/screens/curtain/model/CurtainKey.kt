package app.atomofiron.searchboxapp.screens.curtain.model

typealias CurtainId = String

abstract class CurtainKey {
    val id: CurtainId get() = this::class.java.name
}
