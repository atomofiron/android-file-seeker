package app.atomofiron.common.util.extension

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.res.Resources
import android.widget.Toast
import app.atomofiron.common.util.Alert
import app.atomofiron.common.util.AlertErr
import app.atomofiron.common.util.Android
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.explorer.other.ApkSignature
import app.atomofiron.searchboxapp.model.other.get
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.clipboardAlertsEnabled
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

const val HASH_ALG = "SHA-256"

// fun Context.copy() no-no-no-no

fun ClipboardManager.copy(
    context: Context,
    label: String,
    text: String,
    resources: Resources = context.resources,
    showToast: Boolean = false,
): Alert.Uni? {
    val clip = ClipData.newPlainText(label, text)
    val alert = try {
        setPrimaryClip(clip)
        Alert(R.string.copied)
    } catch (e: Exception) {
        AlertErr(e.toString())
    }
    return when {
        context.clipboardAlertsEnabled() -> null
        !showToast -> alert
        else -> Toast.makeText(context, resources[alert.text], Toast.LENGTH_LONG).show()
            .let { null }
    }
}

fun PackageInfo.signature(): ApkSignature? {
    val signature = when {
        Android.P -> signingInfo?.apkContentsSigners
        else -> @Suppress("DEPRECATION") signatures
    }?.firstOrNull()
    signature ?: return null
    val bytes = signature.toByteArray()
    val factory = CertificateFactory.getInstance("X.509")
    val cert = factory.generateCertificate(signature.toByteArray().inputStream()) as X509Certificate
    val digest = MessageDigest.getInstance(HASH_ALG)
    val hashBytes = digest.digest(bytes)
    val hash = hashBytes.joinToString("") { Const.HEX_BYTE.format(it) }
    return ApkSignature(
        algName = cert.sigAlgName,
        algOID = cert.sigAlgOID,
        issuerName = cert.issuerDN.name,
        since = cert.notBefore.toString(),
        until = cert.notAfter.toString(),
        version = cert.version,
        hashAlg = HASH_ALG,
        hash = hash,
        bytes = bytes.size,
    )
}


