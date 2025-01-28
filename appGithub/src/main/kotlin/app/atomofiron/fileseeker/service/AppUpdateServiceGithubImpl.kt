package app.atomofiron.fileseeker.service

import android.Manifest.permission.REQUEST_INSTALL_PACKAGES
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import app.atomofiron.common.util.isGranted
import app.atomofiron.searchboxapp.BuildConfig
import app.atomofiron.searchboxapp.Unreachable
import app.atomofiron.searchboxapp.android.Intents
import app.atomofiron.searchboxapp.injectable.channel.PreferenceChannel
import app.atomofiron.searchboxapp.injectable.service.ApkService
import app.atomofiron.searchboxapp.injectable.service.AppUpdateService
import app.atomofiron.searchboxapp.injectable.store.AppUpdateStore
import app.atomofiron.searchboxapp.injectable.store.PreferenceStore
import app.atomofiron.searchboxapp.model.network.GithubAsset
import app.atomofiron.searchboxapp.model.network.GithubRelease
import app.atomofiron.searchboxapp.model.network.Loading
import app.atomofiron.searchboxapp.model.other.AppUpdateState
import app.atomofiron.searchboxapp.model.other.UpdateType
import app.atomofiron.searchboxapp.utils.Rslt.Err
import app.atomofiron.searchboxapp.utils.Rslt.Ok
import app.atomofiron.searchboxapp.utils.apkInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File

private const val EXT_APK = ".apk"
private const val SUBDIR = "updates" // src/res/xml/provider_paths.xml

class AppUpdateServiceGithubImpl(
    private val context: Context,
    private val scope: CoroutineScope,
    private val apks: ApkService,
    private val api: UpdateApi,
    private val store: AppUpdateStore,
) : AppUpdateService {
    companion object : AppUpdateService.Factory {
        override fun new(
            context: Context,
            scope: CoroutineScope,
            apkService: ApkService,
            updateStore: AppUpdateStore,
            preferences: PreferenceStore,
            preferenceChannel: PreferenceChannel
        ): AppUpdateService = AppUpdateServiceGithubImpl(
            context,
            scope,
            apkService,
            UpdateApi(),
            updateStore,
        )
    }

    // todo save asset into preferences
    private var asset: GithubAsset? = null
    private var file: File? = null

    override fun onActivityCreate(activity: AppCompatActivity) = Unit

    override fun check(userAction: Boolean) {
        scope.launch {
            when (val releases = api.releases()) {
                is Ok -> releases.data
                    .findAsset(userAction)
                    .also { asset = it }
                    ?.checkFile()
                    ?: AppUpdateState.UpToDate
                is Err -> AppUpdateState.Unknown
            }.let { store.set(it) }
        }
    }

    override fun retry() = when (val state = asset?.checkFile()) {
        null -> check()
        else -> store.set(state)
    }

    override fun startUpdate(variant: UpdateType.Variant) {
        val asset = asset ?: return Unreachable
        file = getFile(asset.id).verify(asset)
        when (file) {
            null -> downloadUpdate(asset)
            else -> store.set(AppUpdateState.Completable)
        }
    }

    override fun completeUpdate() {
        when (val file = file) {
            null -> store.set(asset?.checkFile() ?: AppUpdateState.Unknown)
            else -> {
                val state = store.state.value
                store.set(AppUpdateState.Installing)
                scope.launch {
                    val rslt = apks.installApk(file, action = Intents.ACTION_INSTALL_UPDATE, silently = true)
                    if (rslt is Err) AppUpdateState.Error(rslt.error)
                    if (!context.isGranted(REQUEST_INSTALL_PACKAGES)) {
                        store.set(state)
                    }
                }
            }
        }
    }

    private fun List<GithubRelease>.findAsset(userAction: Boolean) = filter { release -> release.assets.any { it.name.endsWith(EXT_APK) } }
        .maxByOrNull { it.publishedAt }
        ?.takeIf { it.isNewerThan(BuildConfig.UPDATE_THRESHOLD) || userAction && BuildConfig.DEBUG }
        ?.assets
        ?.firstOrNull { it.name.endsWith(EXT_APK) }

    private fun GithubAsset.checkFile(): AppUpdateState {
        file = getFile(id).verify(this)
        return when (file) {
            null -> AppUpdateState.Available(UpdateType.Flexible, id)
            else -> AppUpdateState.Completable
        }
    }

    private fun downloadUpdate(asset: GithubAsset) {
        scope.launch {
            val file = getFile(asset.id)
            file.delete()
            val fallback = store.state.value
            api.download(asset.browserDownloadUrl, file).collect { downloading ->
                when (downloading) {
                    is Loading.Error -> fallback
                    is Loading.Progress -> AppUpdateState.Downloading(downloading.progress)
                    is Loading.Completed -> AppUpdateState.Completable.also {
                        this@AppUpdateServiceGithubImpl.file = file
                    }
                }.let { store.set(it) }
            }
        }
    }

    private fun getFile(id: Int) = File(context.cacheDir, "$SUBDIR/$id$EXT_APK")

    private fun File.verify(asset: GithubAsset): File? = when {
        !exists() -> false
        length() != asset.size -> false
        BuildConfig.DEBUG -> true
        else -> context.apkInfo(path)?.let { it.versionCode > BuildConfig.VERSION_CODE } == true
    }.let { verified ->
        if (!verified) delete()
        takeIf { verified }
    }
}
