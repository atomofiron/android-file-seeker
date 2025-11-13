package app.atomofiron.searchboxapp.model.textviewer

import app.atomofiron.common.util.DoesNotMatterFalse
import app.atomofiron.searchboxapp.model.finder.SearchOptions

data class LocalSearchOptions(
    override val ignoreCase: Boolean = true,
    override val regex: Boolean = false
) : SearchOptions {
    companion object Companion {
        const val DEFAULT = 0
    }
    override val contentSearch: Boolean get() = DoesNotMatterFalse
    override val excludeDirs: Boolean get() = DoesNotMatterFalse

    constructor(value: Int) : this(
        ignoreCase = (value and 1) != 0,
        regex = (value and 1.shl(1)) != 0,
    )

    override fun edit(ignoreCase: Boolean, regex: Boolean, contentSearch: Boolean, excludeDirs: Boolean): SearchOptions {
        return copy(ignoreCase = ignoreCase, regex = regex)
    }

    fun toInt(): Int {
        var value = 0
        if (ignoreCase) value = 1
        if (regex) value += 1 shl 1
        return value
    }
}

fun SearchOptions.toLocal() = when (this) {
    is LocalSearchOptions -> this
    else -> LocalSearchOptions(ignoreCase, regex)
}

