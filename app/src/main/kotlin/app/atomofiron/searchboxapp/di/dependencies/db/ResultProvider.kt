package app.atomofiron.searchboxapp.di.dependencies.db

import android.content.Context
import app.atomofiron.common.util.TaskId
import app.atomofiron.common.util.extension.decodeOrNull
import app.atomofiron.common.util.extension.encode
import app.atomofiron.searchboxapp.di.dependencies.db.dao.FinderDao
import app.atomofiron.searchboxapp.model.finder.GlobalSearchResult
import java.io.File

class ResultProvider(context: Context) {

    private val dir = File(context.cacheDir, FinderDao.DIR_NAME)

    private fun file(id: TaskId) = File(dir, "$id.bin")

    fun store(id: TaskId, result: GlobalSearchResult) {
        dir.mkdirs()
        val file = file(id)
        file.createNewFile()
        val bytes = result.encode()
        file.writeBytes(bytes)
    }

    fun read(id: TaskId): GlobalSearchResult? {
        return file(id)
            .readBytes()
            .decodeOrNull<GlobalSearchResult>()
    }
}