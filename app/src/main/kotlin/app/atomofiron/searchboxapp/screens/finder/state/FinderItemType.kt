package app.atomofiron.searchboxapp.screens.finder.state

import app.atomofiron.common.util.Increment

private val ids = Increment.new()

enum class FinderItemType(val id: Int) {
    FIND(ids()),
    CHARACTERS(ids()),
    TEST(ids()),
    BUTTONS(ids()),
    OPTIONS(ids()),
    MAX_SIZE(ids()),
    MAX_DEPTH(ids()),
    EDIT_CHARS(ids()),
    TITLE(ids()),
    PROGRESS(ids()),
    TARGETS(ids()),
    DISCLAIMER(ids());

    companion object {
        operator fun get(id: Int): FinderItemType? = entries.find { it.id == id }
    }
}