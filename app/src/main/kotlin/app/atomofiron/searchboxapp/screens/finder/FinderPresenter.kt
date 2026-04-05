package app.atomofiron.searchboxapp.screens.finder

import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.flow.collect
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.screens.common.delegates.StoragePermissionDelegate
import app.atomofiron.searchboxapp.screens.finder.adapter.FinderAdapterOutput
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.ButtonsHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.CharactersHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.EditCharactersHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.EditMaxDepthHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.EditMaxSizeHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.EditOptionsHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.QueryFieldHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.TargetsHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.SearchTaskHolder
import app.atomofiron.searchboxapp.screens.finder.adapter.holder.TestHolder
import app.atomofiron.searchboxapp.screens.finder.fragment.history.HistoryAdapter
import app.atomofiron.searchboxapp.screens.finder.presenter.FinderAdapterPresenterDelegate
import app.atomofiron.searchboxapp.screens.finder.presenter.FinderHistoryPresenterDelegate
import app.atomofiron.searchboxapp.screens.finder.presenter.FinderTargetsPresenterDelegate
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject

@FinderScope
class FinderPresenter @Inject constructor(
    scope: CoroutineScope,
    private val viewState: FinderViewState,
    router: FinderRouter,
    private val storagePermissionDelegate: StoragePermissionDelegate,
    finderAdapterDelegate: FinderAdapterPresenterDelegate,
    targetsDelegate: FinderTargetsPresenterDelegate,
    historyDelegate: FinderHistoryPresenterDelegate,
    private val preferenceStore: PreferenceStore,
) : BasePresenter<FinderViewModel, FinderRouter>(scope, router),
    FinderAdapterOutput<SearchResult>,
    QueryFieldHolder.OnActionListener by finderAdapterDelegate,
    CharactersHolder.OnActionListener by finderAdapterDelegate,
    EditCharactersHolder.OnEditCharactersListener by finderAdapterDelegate,
    EditMaxDepthHolder.OnEditMaxDepthListener by finderAdapterDelegate,
    TestHolder.OnTestChangeListener by finderAdapterDelegate,
    EditMaxSizeHolder.OnEditMaxSizeListener by finderAdapterDelegate,
    EditOptionsHolder.FinderConfigListener by finderAdapterDelegate,
    ButtonsHolder.FinderButtonsListener by finderAdapterDelegate,
    SearchTaskHolder.OnActionListener<SearchResult> by finderAdapterDelegate,
    TargetsHolder.FinderTargetsOutput by targetsDelegate,
    HistoryAdapter.OnItemClickListener by historyDelegate
{

    init {
        onSubscribeData()
    }

    override fun onSubscribeData() {
        preferenceStore.drawerGravity.collect(scope) { gravity ->
            viewState.historyDrawerGravity.value = gravity
        }
    }

    fun onDrawerGravityChange(gravity: Int) = preferenceStore { setDrawerGravity(gravity) }

    fun onAllowStorageClick() = storagePermissionDelegate.launchSettings()

    fun onExitAnimationEnd() = router.hide()
}