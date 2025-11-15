package app.atomofiron.searchboxapp.model.explorer

import app.atomofiron.common.util.extension.debug
import kotlinx.serialization.Serializable

private const val SLASH_BYTE = '/'.code.toByte()
private const val DOT_BYTE = '.'.code.toByte()
private val STUB_STRING = String(byteArrayOf())
private val lowerBytes = 97..122 // a-z
private val upperBytes = 65..90 // A-Z
private val digitBytes = 48..57 // 0-9

@Serializable
class NodeRef(val bytes: ByteArray) {
    companion object {
        val Stub = NodeRef(byteArrayOf())
        val Root = NodeRef(byteArrayOf(SLASH_BYTE))
    }

    val isRoot: Boolean get() = bytes.isNotEmpty() && bytes.all { it == SLASH_BYTE }

    var string: String = STUB_STRING
        private set
        get() {
            if (field !== STUB_STRING) return field
            field = String(bytes)
            return field
        }

    var parent: NodeRef = Stub
        private set
        get() {
            if (field !== Stub) return field
            field = bytes.getParent()
            return field
        }

    var name: String = STUB_STRING
        private set
        get() {
            if (field !== STUB_STRING) return field
            field = bytes.getName()
            return field
        }

    var ext: String = STUB_STRING
        private set
        get() {
            if (field !== STUB_STRING) {
                return field
            }
            field = bytes.getExt()
            return field
        }

    val length = bytes.size
    val uniqueId get() = hashCode()

    private var hashCode = 0

    init {
        debug {
            require(bytes.size <= 1 || bytes.last() != SLASH_BYTE) { "why ending slash? $string" }
            var slashes = 0
            for (i in bytes.indices) {
                if (bytes[i] == SLASH_BYTE) slashes++ else slashes = 0
                require(slashes <= 1 || bytes.getOrNull(i - 2) == ':'.code.toByte()) { "too many slashes: $string" }
            }
        }
    }

    constructor(path: String) : this(path.toByteArray())

    constructor(bytes: ByteArray, child: String) : this(bytes + child)

    operator fun plus(child: String) = NodeRef(bytes + child)
        .also { it.parent = this }

    operator fun get(i: Int) = bytes[i]

    fun getOrNull(i: Int) = bytes.getOrNull(i)

    override fun toString(): String = "${this::class.java.simpleName}($string)"

    fun theSame(path: ByteArray): Boolean = bytes.contentEquals(path)

    fun isChildOf(ref: NodeRef): Boolean {
        when {
            ref.length >= length -> return false
            ref.isRoot -> Unit
            get(ref.length) != SLASH_BYTE -> return false
        }
        for (i in ref.bytes.indices) {
            if (ref[i] != bytes[i]) {
                return false
            }
        }
        return true
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is NodeRef -> false
        else -> theSame(other.bytes)
    }

    override fun hashCode(): Int {
        if (hashCode != 0) {
            return hashCode
        }
        hashCode = this::class.hashCode()
        hashCode = 31 * hashCode + bytes.contentHashCode()
        return hashCode
    }
}

private operator fun ByteArray.plus(child: String): ByteArray {
    val child = when {
        child.endsWith('/') -> child.substring(0, child.length.dec())
        else -> child
    }.toByteArray()
    if (child.isEmpty()) {
        return this
    }
    val new = ByteArray(size + child.size + 1)
    copyInto(new)
    new[size] = SLASH_BYTE
    child.copyInto(new, new.size - child.size)
    return new
}

private fun ByteArray.getParent(): NodeRef {
    var end = 0
    var flag = 0
    for (i in indices.reversed()) {
        val byte = get(i)
        val char = byte != SLASH_BYTE
        val chars = (flag % 2) != 0
        // 43333332221111000
        // /sdcard///DCIM///
        if (char != chars && ++flag == 3) {
            end = i
            break
        }
    }
    val bytes = when (flag) {
        0, 1 -> byteArrayOf()
        2 -> byteArrayOf(SLASH_BYTE)
        else -> sliceArray(0..end)
    }
    return NodeRef(bytes)
}

private fun ByteArray.getName(): String {
    var from = size
    var to = 0
    var nextChar = false
    for (i in indices.reversed()) {
        val byte = get(i)
        val char = byte != SLASH_BYTE
        when {
            char && !nextChar -> to = i.inc()
            !char && nextChar -> from = i.inc()
            char && i == 0 -> from = 0
        }
        if (from < to) break
        nextChar = char
    }
    return when {
        from < to -> String(sliceArray(from..<to))
        else -> ""
    }
}

private fun ByteArray.getExt(): String {
    var end = 0
    for (i in indices.reversed()) {
        val char = get(i)
        if (end == 0) {
            when (char) {
                SLASH_BYTE -> continue // skip all ending '/'
                in lowerBytes,
                in upperBytes,
                in digitBytes -> end = i
                else -> break // something wrong, exit
            }
        } else {
            when (char) {
                DOT_BYTE -> return String(sliceArray(i.inc()..end))
                in lowerBytes,
                in upperBytes,
                in digitBytes -> continue
                else -> break // something wrong, exit
            }
        }
    }
    return ""
}
