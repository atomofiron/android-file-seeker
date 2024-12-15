@file:Suppress("PackageDirectoryMismatch")

package debug

open class LeakWatcher {
    open val isAvailable: Boolean = false
    open var isEnabled: Boolean = false
}
