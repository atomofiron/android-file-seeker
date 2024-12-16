package app.atomofiron.searchboxapp.screens.finder.model

import androidx.annotation.StringRes
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.model.explorer.Node
import app.atomofiron.searchboxapp.model.textviewer.SearchTask
import java.util.Objects

// all children mast be 'data classes'
sealed class FinderStateItem(val stableId: Int, val layoutId: Int) {
    companion object {
        private const val SEARCH_ID = 1
        private const val CHARACTERS_ID = 2
        private const val CONFIG_ID = 3
        private const val BUTTONS_ID = 4
        private const val TEST_ID = 5
        private const val DISCLAIMER_ID = 6
        private const val TARGETS_ID = 7
    }

    data class SearchAndReplaceItem(
        var query: String = "", // mutable field
        val replaceEnabled: Boolean = false,
        val useRegex: Boolean = false,
    ) : FinderStateItem(SEARCH_ID, R.layout.item_field_search)

    data class SpecialCharactersItem(
        val characters: Array<String>,
    ) : FinderStateItem(CHARACTERS_ID, R.layout.item_characters) {
        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is SpecialCharactersItem -> false
            else -> characters.contentEquals(other.characters)
        }
        override fun hashCode(): Int = Objects.hash(this::class, characters)
    }

    data class ConfigItem(
      val ignoreCase: Boolean = false,
      val useRegex: Boolean = false,
      val searchInContent: Boolean = false,
      val excludeDirs: Boolean = false,
      val replaceEnabled: Boolean = false,
      val isLocal: Boolean = false,
    ) : FinderStateItem(CONFIG_ID, R.layout.item_search_options)

    data object ButtonsItem : FinderStateItem(BUTTONS_ID, R.layout.item_finder_buttons)

    data class TestItem(
        val searchQuery: String = "",
        val useRegex: Boolean = false,
        val ignoreCase: Boolean = true,
    ) : FinderStateItem(TEST_ID, R.layout.item_test)

    data class ProgressItem(
        val task: SearchTask,
    ) : FinderStateItem(task.uniqueId, R.layout.item_progress)

    data class TargetsItem(
        val targets: List<Node>,
    ) : FinderStateItem(TARGETS_ID, R.layout.item_finder_targets)

    data class TipItem(
        @StringRes val titleId: Int,
    ) : FinderStateItem(titleId.hashCode(), R.layout.item_finder_tip)

    data object DisclaimerItem : FinderStateItem(DISCLAIMER_ID, R.layout.item_finder_disclaimer)
}