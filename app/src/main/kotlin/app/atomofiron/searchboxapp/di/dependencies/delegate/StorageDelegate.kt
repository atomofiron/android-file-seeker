package app.atomofiron.searchboxapp.di.dependencies.delegate

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import androidx.annotation.RequiresApi
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.extension.copy
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeRoot.NodeRootType
import app.atomofiron.searchboxapp.model.explorer.NodeStorage
import app.atomofiron.searchboxapp.utils.ExplorerUtils.completePath
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

class StorageDelegate(
    private val context: Context,
    private val store: ExplorerStore,
) {
    private val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    //private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    //private val statManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager

    private var storageList = mutableListOf<NodeStorage>()
    private val internalStorage = store.internalStorage.value.run {
        val statFs = StatFs(path)
        NodeStorage(NodeStorage.Kind.InternalStorage, path, name, "Internal alias", total = statFs.totalBytes, used = statFs.totalBytes - statFs.freeBytes)
    }

    init {
        store.updateInternalStorage {
            val type = NodeRootType.Storage(internalStorage)
            val content = NodeContent.Directory(rootType = type)
            copy(content = content)
        }
        storageList.add(internalStorage)
        store.setStorage(storageList.copy())
        if (Android.R) {
            storageManager.registerStorageVolumeCallback(Dispatchers.Default.asExecutor(), StorageVolumeCallbackImpl())
            for (volume in storageManager.storageVolumes) {
                val path = volume.directory?.path
                if (path != null && path.completePath(directory = true) != internalStorage.path) {
                    onStateChanged(volume)
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun onStateChanged(volume: StorageVolume) {
        // directory:/mnt/media_rw/3B60-2163 mediaStoreVolumeName:3b60-2163 state:mounted getDescription:Ventoy
        // directory:/mnt/media_rw/3F32-27F5 mediaStoreVolumeName:3f32-27f5 state:mounted getDescription:VTOYEFI
        val index = storageList.indexOfFirst { it.name == volume.mediaStoreVolumeName }
        val alias = volume.getDescription(context)
        val item = storageList.getOrNull(index)
        val path = volume.directory?.path?.completePath(directory = true) ?: item?.path
        val kind = when {
            alias?.contains("SD") == true -> NodeStorage.Kind.SdCard
            else -> NodeStorage.Kind.UsbStorage
        }
        val new = when {
            path == null -> null
            volume.state == Environment.MEDIA_EJECTING -> null
            !storageManager.storageVolumes.contains(volume) -> null
            else -> {
                val statFs = StatFs(path)
                NodeStorage(kind, path, volume.mediaStoreVolumeName, alias, statFs.totalBytes, statFs.totalBytes - statFs.freeBytes)
            }
        }
        when {
            item != null && new != null -> storageList[index] = new
            item != null && new == null -> storageList.removeAt(index)
            item == null && new != null -> storageList.add(new)
            else -> return
        }
        store.setStorage(storageList.copy())
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private inner class StorageVolumeCallbackImpl : StorageManager.StorageVolumeCallback() {
        override fun onStateChanged(volume: StorageVolume) = this@StorageDelegate.onStateChanged(volume)
    }
}