package app.atomofiron.searchboxapp.model.explorer

import kotlinx.coroutines.Job

data class NodeJob(
    val uniqueId: Int,
    val job: Job,
)