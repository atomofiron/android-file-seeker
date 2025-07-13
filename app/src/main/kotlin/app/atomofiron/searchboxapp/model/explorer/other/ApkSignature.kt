package app.atomofiron.searchboxapp.model.explorer.other

// what the heeeeell
interface ApkSignature {
    val issuerName: String
    val algName: String
    val algOID: String
    val since: String
    val until: String
    val version: Int
    val hashAlg: String
    val hash: String
    val bytes: Int

    companion object {
        operator fun invoke(bytes: Int, initializer: () -> ApkSignature) = object : ApkSignature {
            private val data: ApkSignature by lazy(LazyThreadSafetyMode.NONE, initializer)

            override val algName: String get() = data.algName
            override val algOID: String get() = data.algOID
            override val issuerName: String get() = data.issuerName
            override val since: String get() = data.since
            override val until: String get() = data.until
            override val version: Int get() = data.version
            override val hashAlg: String get() = data.hashAlg
            override val hash: String get() = data.hash
            override val bytes: Int = bytes

            override fun equals(other: Any?): Boolean = when (other) {
                null, !is ApkSignature -> false
                else -> bytes == other.bytes
            }

            override fun toString(): String = "ApkSignature($bytes)"
        }
    }
}
