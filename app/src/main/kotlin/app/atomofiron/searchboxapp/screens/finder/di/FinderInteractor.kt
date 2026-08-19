package app.atomofiron.searchboxapp.screens.finder.di

import app.atomofiron.searchboxapp.di.dependencies.service.FinderService
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.finder.GenericSearchTask
import app.atomofiron.searchboxapp.model.finder.SearchOptions
import app.atomofiron.searchboxapp.screens.finder.FinderScope
import app.atomofiron.searchboxapp.utils.CoroutineLauncher
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import kotlin.uuid.Uuid

@FinderScope
class FinderInteractor @Inject constructor(
    scope: CoroutineScope,
    private val finderService: FinderService,
) : CoroutineLauncher by CoroutineLauncher(scope) {

    fun search(query: String, where: List<NodeRef>, config: SearchOptions) = default {
        finderService.search(query, where, config)
    }

    fun stop(uuid: Uuid) = finderService.stop(uuid)

    fun drop(task: GenericSearchTask) = default {
        finderService.drop(task)
    }
}