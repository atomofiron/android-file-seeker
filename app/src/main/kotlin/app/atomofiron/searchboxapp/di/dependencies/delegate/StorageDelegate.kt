package app.atomofiron.searchboxapp.di.dependencies.delegate

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import androidx.annotation.RequiresApi
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.extension.copy
import app.atomofiron.searchboxapp.di.dependencies.store.ExplorerStore
import app.atomofiron.searchboxapp.model.explorer.NodeStorage
import app.atomofiron.searchboxapp.model.explorer.NodeStorage.Kind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageDelegate @Inject constructor(
    private val context: Context,
    private val store: ExplorerStore,
) {
    private val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
    //private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    //private val statManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager

    private var storageList = mutableListOf<NodeStorage>()
    private val internalStorage = Environment.getExternalStorageDirectory()
        .takeIf { it.exists() }
        ?.run { NodeStorage(Kind.InternalStorage, absolutePath, name, "unused for internal storage") }

    init {
        if (internalStorage != null) {
            storageList.add(internalStorage)
            store.setMainStorage(internalStorage)
        }
        store.setStorage(storageList.copy())
        if (Android.R) {
            storageManager.registerStorageVolumeCallback(Dispatchers.Default.asExecutor(), StorageVolumeCallbackImpl())
            for (volume in storageManager.storageVolumes) {
                val path = volume.directory?.path
                if (path != null && path != internalStorage?.path) {
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
        val path = volume.directory?.path ?: item?.path
        val kind = when {
            !volume.isRemovable -> Kind.InternalStorage
            alias?.contains("SD") == true -> Kind.SdCard
            else -> Kind.UsbStorage
        }
        val new = when {
            path == null -> null
            volume.state == Environment.MEDIA_EJECTING -> null
            !storageManager.storageVolumes.contains(volume) -> null
            else -> NodeStorage(kind, path, volume.mediaStoreVolumeName, alias)
        }
        if (internalStorage == null && kind == Kind.SdCard && store.mainStorage.value?.path != new?.path) {
            store.setMainStorage(new)
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