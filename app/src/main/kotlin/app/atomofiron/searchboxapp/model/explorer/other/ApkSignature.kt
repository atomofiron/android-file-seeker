package app.atomofiron.searchboxapp.model.explorer.other

data class ApkSignature(
    val issuerName: String,
    val algName: String,
    val algOID: String,
    val since: String,
    val until: String,
    val version: Int,
    val hashAlg: String,
    val hash: String,
    val bytes: Int,
)
