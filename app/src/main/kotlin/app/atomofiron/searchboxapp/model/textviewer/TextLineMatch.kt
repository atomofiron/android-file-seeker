package app.atomofiron.searchboxapp.model.textviewer

import uniffi.native_lib.TextMatch

// line index -> byte offset + length

typealias MutableMatchMap = MutableMap<Int, MutableMatchList>

typealias MutableMatchList = MutableList<TextMatch>

typealias MatchMap = Map<Int, MatchList>

typealias MatchList = List<TextMatch>
