package app.atomofiron.searchboxapp.model.finder

data class SearchConfig(
    override val ignoreCase: Boolean = true,
    override val useRegex: Boolean = false,
    override val searchInContent: Boolean = false,
    override val excludeDirs: Boolean = false,
    override val replaceEnabled: Boolean = false,
) : ISearchConfig

interface ISearchConfig {
    val ignoreCase: Boolean
    val useRegex: Boolean
    val searchInContent: Boolean
    val excludeDirs: Boolean
    val replaceEnabled: Boolean
}