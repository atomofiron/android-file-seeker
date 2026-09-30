package app.atomofiron.searchboxapp.screens.finder.presenter

import app.atomofiron.searchboxapp.screens.finder.FinderScope
import app.atomofiron.searchboxapp.screens.finder.FinderViewState
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.CharsetsHolder
import javax.inject.Inject

@FinderScope
class FinderCharsetsPresenterDelegate @Inject constructor(
    private val viewState: FinderViewState,
) : CharsetsHolder.CharsetsOutput {

    override fun onCharsetClick(charset: String, select: Boolean) {
        if (select) viewState.setCharset(charset)
    }
}
