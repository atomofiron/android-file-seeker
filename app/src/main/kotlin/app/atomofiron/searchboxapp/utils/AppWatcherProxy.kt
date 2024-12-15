@file:Suppress("PackageDirectoryMismatch")

package debug

open class AppWatcherProxy {
    open val isAvailable: Boolean = false
    open var isEnabled: Boolean = false
}
