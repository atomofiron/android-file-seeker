package app.atomofiron.searchboxapp.screens.licenses.di

import android.content.res.AssetManager
import app.atomofiron.searchboxapp.screens.licenses.LicensesScope
import app.atomofiron.searchboxapp.screens.licenses.state.License
import app.atomofiron.searchboxapp.screens.licenses.state.LicenseContent
import app.atomofiron.searchboxapp.utils.Const.LF
import javax.inject.Inject
import kotlin.collections.component3
import kotlin.text.Charsets.UTF_8

private const val LF_BYTE = LF.code.toByte()

@LicensesScope
class LicensesService @Inject constructor(
    private val assets: AssetManager,
) {

    fun readLicences(): List<License> {
        val metadata = assets.open("licenses/third_party_license_metadata")
            .bufferedReader()
            .readLines()
        val licenses = assets.open("licenses/third_party_licenses")
            .readBytes()
        val delimiters = Regex("[: ]")
        return metadata.mapNotNull { line ->
            line.split(delimiters, limit = 3)
                .takeIf { it.size == 3 }
        }.map { (index, length, name) ->
            var start = index.toInt()
            while (licenses[start] == LF_BYTE) {
                start++
            }
            val text = String(licenses, index.toInt(), length.toInt(), UTF_8)
            val content = when {
                text.contains(LF) -> LicenseContent.Text(text)
                else -> LicenseContent.Url(text)
            }
            License(name, content)
        }
    }
}
