package app.atomofiron.common.util.extension

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.res.Resources
import android.widget.Toast
import app.atomofiron.common.util.Android
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.model.explorer.other.ApkSignature
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
) {
    val clip = ClipData.newPlainText(label, text)
    val toast = try {
        setPrimaryClip(clip)
        resources.getString(R.string.copied)
    } catch (e: Exception) {
        e.toString()
    }
    Toast.makeText(context, toast, Toast.LENGTH_LONG).show()
}

fun PackageInfo.signature(): ApkSignature? {
    val signature = when {
        Android.P -> signingInfo?.apkContentsSigners
        else -> @Suppress("DEPRECATION") signatures
    }?.firstOrNull()
    signature ?: return null
    val bytes = signature.toByteArray()
    return ApkSignature.invoke(bytes.size) {
        val factory = CertificateFactory.getInstance("X.509")
        val cert = factory.generateCertificate(signature.toByteArray().inputStream()) as X509Certificate
        val digest = MessageDigest.getInstance(HASH_ALG)
        val hashBytes = digest.digest(bytes)
        val hash = hashBytes.joinToString("") { "%02x".format(it) }
        object : ApkSignature {
            override val algName = cert.sigAlgName
            override val algOID = cert.sigAlgOID
            override val issuerName = cert.issuerDN.name
            override val since = cert.notBefore.toString()
            override val until = cert.notAfter.toString()
            override val version = cert.version
            override val hashAlg = HASH_ALG
            override val hash = hash
            override val bytes = bytes.size
        }
    }
}
