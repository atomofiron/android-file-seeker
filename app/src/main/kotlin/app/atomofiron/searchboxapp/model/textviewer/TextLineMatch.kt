package app.atomofiron.searchboxapp.model.textviewer

// line index -> byteOffset + length

typealias MutableMatchMap = MutableMap<Int, MutableMatchList>

typealias MutableMatchList = MutableList<TextLineMatch>

typealias MatchMap = Map<Int, MatchList>

typealias MatchList = List<TextLineMatch>

data class TextLineMatch(
    val byteOffset: Long,
    val length: Int,
)
