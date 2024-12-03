package app.atomofiron.searchboxapp.screens.finder.model

import app.atomofiron.searchboxapp.R

enum class FinderItemType(val id: Int) {
    FIND(R.layout.item_field_search),
    CHARACTERS(R.layout.item_characters),
    CONFIGS(R.layout.item_search_options),
    TEST(R.layout.item_test),
    BUTTONS(R.layout.item_finder_buttons),
    PROGRESS(R.layout.item_progress),
    TARGETS(R.layout.item_finder_targets),
    TIP(R.layout.item_finder_tip),
    DISCLAIMER(R.layout.item_finder_disclaimer),
}