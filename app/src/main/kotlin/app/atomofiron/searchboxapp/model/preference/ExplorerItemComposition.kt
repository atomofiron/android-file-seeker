package app.atomofiron.searchboxapp.model.preference

data class ExplorerItemComposition(
    val visibleAccess: Boolean,
    val visibleOwner: Boolean,
    val visibleGroup: Boolean,
    val visibleDate: Boolean,
    val visibleTime: Boolean,
    val visibleSize: Boolean,
    val visibleBox: Boolean,
    val visibleBg: Boolean,
) {
    companion object {
        private const val ACCESS = 0b00000001
        private const val OWNER = 0b00000010
        private const val GROUP = 0b00000100
        private const val DATE = 0b00001000
        private const val TIME = 0b00010000
        private const val SIZE = 0b00100000
        private const val BOX = 0b01000000
        private const val BG = 0b10000000
    }

    constructor(flags: Int) : this(
        flags and ACCESS == ACCESS,
        flags and OWNER == OWNER,
        flags and GROUP == GROUP,
        flags and DATE == DATE,
        flags and TIME == TIME,
        flags and SIZE == SIZE,
        flags and BOX == BOX,
        flags and BG == BG,
    )

    val flags: Int get() {
        var flags = 0
        if (visibleAccess) flags += ACCESS
        if (visibleOwner) flags += OWNER
        if (visibleGroup) flags += GROUP
        if (visibleDate) flags += DATE
        if (visibleTime) flags += TIME
        if (visibleSize) flags += SIZE
        if (visibleBox) flags += BOX
        if (visibleBg) flags += BG
        return flags
    }
}