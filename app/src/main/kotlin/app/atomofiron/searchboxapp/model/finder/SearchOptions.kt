package app.atomofiron.searchboxapp.model.finder

interface SearchOptions {
    companion object Companion {
        const val DEFAULT = 0
    }
    val ignoreCase: Boolean
    val regex: Boolean
    val contentSearch: Boolean
    val excludeDirs: Boolean

    fun edit(
        ignoreCase: Boolean = this.ignoreCase,
        regex: Boolean = this.regex,
        contentSearch: Boolean = this.contentSearch,
        excludeDirs: Boolean = this.excludeDirs,
    ): SearchOptions
}

data class SearchOptionsImpl(
    override val ignoreCase: Boolean = true,
    override val regex: Boolean = false,
    override val contentSearch: Boolean = false,
    override val excludeDirs: Boolean = false,
) : SearchOptions {

    constructor(value: Int) : this(
        ignoreCase = (value and 1) != 0,
        regex = (value and 1.shl(1)) != 0,
        contentSearch = (value and 1.shl(2)) != 0,
        excludeDirs = (value and 1.shl(3)) != 0,
    )

    override fun edit(ignoreCase: Boolean, regex: Boolean, contentSearch: Boolean, excludeDirs: Boolean): SearchOptions {
        return copy(ignoreCase, regex, contentSearch, excludeDirs)
    }
}

fun SearchOptions.toInt(): Int {
    var value = 0
    if (ignoreCase) value = 1
    if (regex) value += 1 shl 1
    if (contentSearch) value += 1 shl 2
    if (excludeDirs) value += 1 shl 3
    return value
}
