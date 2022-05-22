package app.atomofiron.searchboxapp.injectable.service.explorer

import android.content.Context
import android.content.SharedPreferences
import android.content.res.AssetManager
import app.atomofiron.searchboxapp.injectable.store.ExplorerStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.logI
import app.atomofiron.searchboxapp.model.explorer.MutableXFile
import app.atomofiron.searchboxapp.model.explorer.XFile
import app.atomofiron.searchboxapp.utils.Const

class ExplorerService(
    context: Context,
    assets: AssetManager,
    private val preferences: SharedPreferences,
    explorerStore: ExplorerStore,
    preferenceStore: PreferenceStore
) : PrivateExplorerServiceLogic(context, assets, explorerStore, preferenceStore) {

    fun persistState() {
        preferences.edit().putString(Const.PREF_CURRENT_DIR, currentOpenedDir?.completedPath).apply()
    }

    suspend fun setRoots(vararg path: String) {
        val roots = path.map { MutableXFile.asRoot(it) }
        logI("setRoots ${roots.size}")
        super.setRoots(roots)
    }

    fun invalidateDir(dir: XFile) {
        val item = findItem(dir)
        item ?: return logI("invalidateDir $item")
        super.invalidateDir(item)
    }

    suspend fun open(it: XFile) {
        val item = findItem(it)
        item ?: return logI("open $item")
        super.open(item)
    }

    suspend fun openParent() {
        val dir = currentOpenedDir
        dir ?: return logI("openParent $dir")
        super.closeDir(dir)
    }

    suspend fun updateItem(it: XFile) {
        val item = findItem(it)
        item ?: return logI("updateItem $item")
        super.updateItem(item)
    }

    fun checkItem(it: XFile, isChecked: Boolean) {
        val item = findItem(it)
        item ?: return logI("checkItem $isChecked $item")
        super.checkItem(item, isChecked)
    }

    suspend fun delete(its: List<XFile>) {
        logI("deleteItems ${its.size}")
        val items = its.mapNotNull { findItem(it) }
        super.deleteItems(items)
    }

    suspend fun rename(it: XFile, name: String) {
        val item = findItem(it)
        item ?: return logI("rename $name $item")
        super.rename(item, name)
    }

    suspend fun create(it: XFile, name: String, directory: Boolean) {
        val dir = findItem(it)
        dir ?: return logI("create $name $dir")
        super.create(dir, name, directory)
    }
}