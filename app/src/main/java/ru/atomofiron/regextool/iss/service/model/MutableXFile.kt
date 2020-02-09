package ru.atomofiron.regextool.iss.service.model

import ru.atomofiron.regextool.utils.Shell
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MutableXFile : XFile {
    companion object {
        private const val SLASH = "/"
        private const val ROOT = "/"
        private const val TOTAL = "total"
        private const val DIR_CHAR = 'd'
        private const val LINK_CHAR = 'l'
        private const val FILE_CHAR = '-'
        private const val NO_SUCH_FILE = "ls: %s: No such file or directory\n"

        var toyboxPath: String = ""
        private val spaces = Regex(" +")
        private val parentSuffix = Regex("(?<=/)/*[^/]+/*$")
        private val lastOneSlash = Regex("/*$")

        fun completePath(absolutePath: String, isDirectory: Boolean = true): String {
            return when {
                absolutePath == ROOT -> ROOT
                isDirectory -> absolutePath.replace(lastOneSlash, SLASH)
                else -> absolutePath
            }
        }

        fun completePathAsDir(absolutePath: String): String = completePath(absolutePath)

        fun byPath(absolutePath: String): MutableXFile {
            val file = File(absolutePath)
            return MutableXFile("", "", "", "", "", "", file.name, "", file.isDirectory, file.absolutePath)
        }

        private fun parse(completedParentPath: String, line: String): MutableXFile {
            val parts = line.split(spaces, 8)
            val access = parts[0]
            val owner = parts[2]
            val group = parts[3]
            val size = parts[4]
            val date = parts[5]
            val time = parts[6]
            val name = parts[7]
            val isDirectory = access[0] == DIR_CHAR

            if (parts[7].contains('>')) {
                // todo links
            }

            val suffix = ""
            val absolutePath = completedParentPath + name

            return MutableXFile(access, owner, group, size, date, time, name, suffix, isDirectory, absolutePath)
        }
    }
    override var files: MutableList<MutableXFile>? = null
        private set

    override var isOpened: Boolean = false
        private set(value) {
            require(isDirectory || !value) { "$completedPath is not a directory!" }
            field = value
        }

    private var dropCaching: Boolean = false
    private var isCaching: Boolean = false
    override val isCached: Boolean get() = files != null
    override var isCacheActual: Boolean = false
        private set

    override var completedPath: String private set
    override var completedParentPath: String private set

    override var access: String private set
    override var owner: String private set
    override var group: String private set
    override var size: String private set
    override var date: String private set
    override var time: String private set
    override val name: String
    override val suffix: String
    override var exists: Boolean = true
        private set

    override var isDirectory: Boolean private set
    override var isFile: Boolean private set

    @Suppress("ConvertSecondaryConstructorToPrimary")
    constructor(access: String, owner: String, group: String, size: String, date: String, time: String,
                name: String, suffix: String, isDirectory: Boolean, absolutePath: String) {
        this.access = access
        this.owner = owner
        this.group = group
        this.size = size
        this.date = date
        this.time = time
        this.name = name
        this.suffix = suffix
        this.isDirectory = isDirectory
        this.isFile = !isDirectory && (access.isEmpty() || access[0] == FILE_CHAR)

        completedPath = completePath(absolutePath, isDirectory)
        completedParentPath = completedPath.replace(parentSuffix, "")
    }

    fun open() {
        isOpened = true
    }

    fun close() {
        isOpened = false
    }

    fun clear() {
        dropCaching = true
        isCacheActual = false
        files = null
        isOpened = false
    }

    fun clearChildren() {
        files!!.forEach { it.clear() }
    }

    fun invalidateCache() {
        isCacheActual = false
    }

    /** @return error or null */
    fun updateCache(su: Boolean = false): String? {
        dropCaching = false
        when {
            isCaching -> return "Cache in process. $this"
            isCacheActual -> return "Cache is actual. $this"
        }
        return when {
            isDirectory -> cacheAsDir(su)
            else -> cacheAsFile(su)
        }
    }

    private fun cacheAsDir(su: Boolean = false): String? {
        isCaching = true
        sleep()
        val output = Shell.exec(Shell.LS_LAHL.format(toyboxPath, completedPath), su)
        return when {
            dropCaching -> {
                isCaching = false
                return "Dir was cleared. $this"
            }
            output.success -> {
                val lines = output.output.split("\n")
                val dirs = ArrayList<MutableXFile>()
                val files = ArrayList<MutableXFile>()

                if (lines.isNotEmpty()) {
                    var start = 0
                    if (lines[start].startsWith(TOTAL)) {
                        start++
                    }
                    for (i in start until lines.size) {
                        if (lines[i].isNotEmpty()) {
                            val file = parse(completedPath, lines[i])
                            if (file.isDirectory) {
                                dirs.add(file)
                            } else {
                                files.add(file)
                            }
                        }
                    }
                }

                dirs.sortBy { it.name.toLowerCase(Locale.ROOT) }
                files.sortBy { it.name.toLowerCase(Locale.ROOT) }
                files.addAll(0, dirs)

                val oldFiles = this.files
                this.files = files
                persistOldFiles(oldFiles)
                isCaching = false
                isCacheActual = true
                null
            }
            else -> {
                parseDoesExists(output.error)
                files = ArrayList()
                isCaching = false
                isCacheActual = true
                output.error
            }
        }
    }

    private fun sleep() = Unit//Thread.sleep(300 + (Math.random() * 700).toLong())

    private fun persistOldFiles(oldFiles: MutableList<MutableXFile>?) {
        val newFiles = this.files!!
        if (oldFiles != null) {
            newFiles.forEachIndexed { newIndex, new ->
                if (new.isDirectory) {
                    val lastIndex = oldFiles.indexOf(new)
                    if (lastIndex != -1) {
                        val last = oldFiles[lastIndex]
                        last.updateData(new)
                        newFiles[newIndex] = last
                    }
                }
            }
        }
    }

    private fun cacheAsFile(su: Boolean = false): String? {
        isCaching = true
        sleep()
        val output = Shell.exec(Shell.LS_LAHL.format(toyboxPath, completedPath), su)
        val line = output.output.replace("\n", "")
        if (output.success && line.isNotEmpty()) {
            val parts = line.split(spaces, 8)
            access = parts[0]
            owner = parts[2]
            group = parts[3]
            size = parts[4]
            date = parts[5]
            time = parts[6]
            isDirectory = access[0] == DIR_CHAR
            isFile = !isDirectory && access[0] == FILE_CHAR
        }
        isCaching = false
        isCacheActual = true

        return when (output.success) {
            true -> null
            false -> {
                parseDoesExists(output.error)
                output.error
            }
        }
    }

    private fun parseDoesExists(error: String) {
        if (error == NO_SUCH_FILE.format(completedPath)) {
            exists = false
        }
    }

    private fun updateData(file: MutableXFile) {
        access = file.access
        owner = file.owner
        group = file.group
        size = file.size
        date = file.date
        time = file.time
        isDirectory = file.isDirectory
        isFile = !isDirectory && (access.isEmpty() || access[0] == FILE_CHAR)
    }

    override fun equals(other: Any?): Boolean {
        return when {
            other == null -> false
            other !is MutableXFile -> false
            other.isDirectory != isDirectory -> false
            else -> other.completedPath == completedPath
        }
    }

    override fun hashCode(): Int = completedPath.hashCode()

    override fun toString(): String = completedPath
}