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
    val visibleDetails: Boolean,
) {
    companion object {
        private const val ACCESS =  0b000000001
        private const val OWNER =   0b000000010
        private const val GROUP =   0b000000100
        private const val DATE =    0b000001000
        private const val TIME =    0b000010000
        private const val SIZE =    0b000100000
        private const val BOX =     0b001000000
        private const val BG =      0b010000000
        private const val DETAILS = 0b100000000

        const val DEFAULT = DATE or TIME or SIZE or BOX or BG or DETAILS
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
        flags and DETAILS == DETAILS,
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
        if (visibleDetails) flags += DETAILS
        return flags
    }
}