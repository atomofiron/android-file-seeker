package app.atomofiron.searchboxapp.screens.result

import androidx.core.os.ConfigurationCompat
import app.atomofiron.common.arch.BasePresenter
import app.atomofiron.common.util.AlertMessage
import app.atomofiron.common.util.flow.collect
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.injectable.interactor.ResultInteractor
import app.atomofiron.searchboxapp.injectable.store.*
import app.atomofiron.searchboxapp.logE
import app.atomofiron.searchboxapp.model.finder.SearchResult
import app.atomofiron.searchboxapp.screens.result.adapter.ResultItemActionListener
import app.atomofiron.searchboxapp.screens.result.presenter.ResultItemActionDelegate
import app.atomofiron.searchboxapp.screens.result.presenter.ResultPresenterParams
import app.atomofiron.searchboxapp.utils.Const
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.*

class ResultPresenter(
    params: ResultPresenterParams,
    scope: CoroutineScope,
    private val viewState: ResultViewState,
    private val finderStore: FinderStore,
    private val preferenceStore: PreferenceStore,
    private val interactor: ResultInteractor,
    router: ResultRouter,
    appStore: AppStore,
    itemActionDelegate: ResultItemActionDelegate,
) : BasePresenter<ResultViewModel, ResultRouter>(scope, router),
    ResultItemActionListener by itemActionDelegate {
    companion object {
        private const val UNDEFINED = -1
    }
    private val taskId = params.taskId
    private val resources by appStore.resourcesProperty

    init {
        val task = finderStore.tasks.find { it.uniqueId == taskId }
        if (task == null) {
            logE("No task found!")
            router.navigateBack()
        } else {
            viewState.task.value = task
        }
        onSubscribeData()
    }

    override fun onSubscribeData() {
        if (taskId != UNDEFINED) finderStore.tasksFlow.combine(viewState.checked) { tasks, checked ->
            tasks.find { it.uniqueId == taskId }?.let { task ->
                val result = task.result as SearchResult.FinderResult
                val matches = result.matches.map { match ->
                    when {
                        !checked.contains(match.item.uniqueId) -> match
                        else -> match.update(match.item.copy(isChecked = true))
                    }
                }
                task.copy(result = task.result.copy(matches = matches))
            }
        }.collect(scope) { task ->
            task ?: return@collect
            viewState.task.value = task
        }
        preferenceStore.explorerItemComposition.collect(scope) {
            viewState.composition.value = it
        }
    }

    fun onStopClick() = interactor.stop(viewState.task.value.uuid)

    fun onShareClick() {
        val task = viewState.task.value
        val data = task.result as SearchResult.FinderResult
        val items = data.matches.map { it.item }
        router.shareWith(items)
    }

    fun onExportClick() {
        val task = viewState.task.value
        val data = (task.result as SearchResult.FinderResult).toMarkdown()
        val locale = ConfigurationCompat.getLocales(resources.configuration)[0]
        val date = SimpleDateFormat(Const.DATE_PATTERN, locale).format(Date())
        val title = "search_$date.md.txt";

        if (!router.shareFile(title, data)) {
            viewState.showAlert(AlertMessage(R.string.no_activity, important = true))
        }
    }
}
