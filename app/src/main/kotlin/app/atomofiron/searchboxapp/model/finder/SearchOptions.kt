package app.atomofiron.searchboxapp.model.finder

data class SearchOptions(
    override val ignoreCase: Boolean = true,
    override val regex: Boolean = false,
    override val contentSearch: Boolean = false,
    override val excludeDirs: Boolean = false,
) : ISearchConfig {
    companion object Companion {
        const val DEFAULT = 0
    }

    constructor(value: Int) : this(
        ignoreCase = (value and 1) != 0,
        regex = (value and 1.shl(1)) != 0,
        contentSearch = (value and 1.shl(2)) != 0,
        excludeDirs = (value and 1.shl(3)) != 0,
    )

    fun toInt(): Int {
        var value = 0
        if (ignoreCase) value = 1
        if (regex) value += 1 shl 1
        if (contentSearch) value += 1 shl 2
        if (excludeDirs) value += 1 shl 3
        return value
    }
}

interface ISearchConfig {
    val ignoreCase: Boolean
    val regex: Boolean
    val contentSearch: Boolean
    val excludeDirs: Boolean
}