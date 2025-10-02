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
class NodePath(val bytes: ByteArray) {
    companion object {
        val Stub = NodePath(byteArrayOf())
        val Root = NodePath(byteArrayOf(SLASH_BYTE))
    }

    var string: String = STUB_STRING
        private set
        get() {
            if (field !== STUB_STRING) return field
            field = String(bytes)
            return field
        }

    var parent: NodePath = Stub
        private set
        get() {
            if (field !== Stub) return field
            field = bytes.getParent()
            return field
        }

    var parentString: String = STUB_STRING
        private set
        get() {
            if (field !== STUB_STRING) return field
            field = String(parent.bytes)
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
        }
    }

    constructor(path: String) : this(path.toByteArray())
    constructor(bytes: ByteArray, child: String) : this(bytes + child)

    operator fun plus(child: String) = NodePath(bytes + child)

    operator fun get(i: Int) = bytes[i]

    fun getOrNull(i: Int) = bytes.getOrNull(i)

    override fun toString(): String = "NodePath($string)"

    fun theSame(path: ByteArray): Boolean = bytes.contentEquals(path)

    fun isChildOf(path: NodePath): Boolean {
        if (path.length > length - 2 || get(path.length) != SLASH_BYTE) {
            return false
        }
        for (i in path.bytes.indices) {
            if (path[i] != bytes[i]) {
                return false
            }
        }
        return true
    }

    override fun equals(other: Any?): Boolean = when {
        this === other -> true
        other !is NodePath -> false
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
    val addSlash = when (size) {
        0 -> false
        1 -> first() != SLASH_BYTE
        else -> true
    }
    val new = ByteArray(size + child.length + if (addSlash) 1 else 0)
    copyInto(new)
    if (addSlash) new[size] = SLASH_BYTE
    child.toByteArray().copyInto(new, new.size - child.length)
    return new
}

private fun ByteArray.getParent(): NodePath {
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
    return NodePath(bytes)
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
        from < to -> String(sliceArray(from..to.dec()))
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
