package app.atomofiron.searchboxapp.screens.common.delegates

import android.content.ContentResolver
import androidx.core.net.toUri
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.UnreachableException
import app.atomofiron.common.util.dialog.DialogConfig
import app.atomofiron.common.util.dialog.DialogDelegate
import app.atomofiron.common.util.extension.then
import app.atomofiron.common.util.extension.withMain
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.di.dependencies.delegate.ApkDelegate
import app.atomofiron.searchboxapp.di.dependencies.store.PreferenceStore
import app.atomofiron.searchboxapp.model.explorer.NodeContent
import app.atomofiron.searchboxapp.model.explorer.NodeRef
import app.atomofiron.searchboxapp.model.explorer.NodeTabKey
import app.atomofiron.searchboxapp.model.explorer.other.ApkInfo
import app.atomofiron.searchboxapp.model.other.UniText
import app.atomofiron.searchboxapp.utils.ExplorerUtils.isContent
import app.atomofiron.searchboxapp.utils.Rslt
import app.atomofiron.searchboxapp.utils.getApksContent
import app.atomofiron.searchboxapp.utils.getAppContent
import app.atomofiron.searchboxapp.utils.unwrapOrElse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

class ApkOperationsDelegate @Inject constructor(
    private val apks: ApkDelegate,
    private val dialogs: DialogDelegate,
    preferences: PreferenceStore,
) {
    private val asSu by preferences.asSu

    fun askForAndroidApp(ref: NodeRef, content: NodeContent.AndroidApp, tab: NodeTabKey? = null) = askForAndroidApp(ref, content, contentResolver = null, tab)

    fun askForApks(ref: NodeRef, contentResolver: ContentResolver) = askForAndroidApp(ref, NodeContent.AndroidApp.Apks, contentResolver)

    private fun askForAndroidApp(
        ref: NodeRef,
        content: NodeContent.AndroidApp,
        contentResolver: ContentResolver?,
        tab: NodeTabKey? = null,
    ) {
        var content = content
        var scope: CoroutineScope? = null
        val updater = dialogs show DialogConfig(
            cancelable = content.info != null,
            negative = DialogDelegate.Cancel,
            positive = UniText(R.string.install),
            onPositiveClick = { apks.install(ref, content, tab) },
            onDismiss = { scope?.cancel() },
        ).update(content)
        updater ?: return
        scope = CoroutineScope(Job())
        val job = scope.launch {
            val forSignature = content.info != null
            var result = content.resolve(ref, contentResolver, signature = forSignature)
            withMain {
                content = result.unwrapOrElse {
                    if (!forSignature) updater.showError(UniText(it))
                    return@withMain
                }
                updater.update { update(content, forSignature) }
            }
            if (forSignature) {
                return@launch
            }
            result = content.resolve(ref, contentResolver, signature = true)
            withMain {
                content = result.unwrapOrElse {
                    return@withMain
                }
                updater.update { update(content, withSignature = true) }
            }
        }
        scope.launch {
            job.join()
            scope.cancel()
        }
    }

    private suspend fun NodeContent.AndroidApp.resolve(ref: NodeRef, resolver: ContentResolver?, signature: Boolean): Rslt<NodeContent.AndroidApp> {
        val stream = when {
            !ref.isContent() -> return getAppContent(ref, asSu = asSu, signature = signature)
            resolver == null -> throw UnreachableException()
            else -> resolver.openInputStream(ref.string.toUri())
        }
        return getApksContent(stream, signature = signature)
    }

    private fun DialogConfig.update(content: NodeContent.AndroidApp, withSignature: Boolean = false): DialogConfig = copy(
        cancelable = content.info != null,
        icon = if (content.info == null) dialogs.loadingIcon() else content.info.icon?.drawable,
        title = UniText(content.info?.appName) ?: UniText(R.string.fetching),
        message = content.info.toMessage(withSignature),
        neutral = apks.launchable(content.info) then { UniText(R.string.launch) to { apks.launch(content.info) } },
    )

    private fun ApkInfo?.toMessage(withSignature: Boolean): UniText {
        val args = if (this == null) {
            val ellipsis = dialogs[UniText(R.string.ellipsis)]
            Array(6) { ellipsis }.toList()
        } else {
            val unavailable = dialogs[UniText(R.string.unavailable)]
            listOf(
                packageName,
                "$versionName ($versionCode)",
                "$minSdkVersion (${Android[minSdkVersion] ?: unavailable})",
                "$targetSdkVersion (${Android[targetSdkVersion] ?: unavailable})",
                compileSdkVersion
                    ?.let { "$it (${Android[it] ?: unavailable})" }
                    ?: unavailable,
                signature
                    ?.let { "v${it.version}\n${it.issuerName}\n${it.hashAlg}: ${it.hash}" }
                    ?: unavailable.takeIf { withSignature }
                    ?: dialogs[UniText(R.string.ellipsis)],
            )
        }
        return UniText(R.string.apk_info, args)
    }
}