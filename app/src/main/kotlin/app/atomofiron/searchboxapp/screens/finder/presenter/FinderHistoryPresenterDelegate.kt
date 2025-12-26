package app.atomofiron.searchboxapp.screens.finder.presenter

import app.atomofiron.common.util.extension.unit
import app.atomofiron.searchboxapp.screens.finder.FinderScope
import app.atomofiron.searchboxapp.screens.finder.FinderViewState
import app.atomofiron.searchboxapp.screens.finder.fragment.history.HistoryAdapter
import app.atomofiron.searchboxapp.screens.finder.di.history.HistoryDao
import app.atomofiron.searchboxapp.screens.finder.di.history.ItemHistory
import app.atomofiron.searchboxapp.utils.CoroutineLauncher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@FinderScope
class FinderHistoryPresenterDelegate @Inject constructor(
    override val scope: CoroutineScope,
    private val viewState: FinderViewState,
    private val history: HistoryDao,
) : HistoryAdapter.OnItemClickListener, CoroutineLauncher {

    override fun onItemClick(item: ItemHistory) = viewState.replaceQuery(item.query)

    override fun onItemPin(item: ItemHistory) = io {
        history.put(item.copy(pinned = !item.pinned)).unit()
    }.unit()

    override fun onItemRemove(item: ItemHistory) = io {
        history.delete(item)
    }.unit()
}
