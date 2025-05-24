package app.atomofiron.common.util

interface DialogUpdater {
    fun update(action: DialogConfig.() -> DialogConfig)
    fun showError(message: String?)
}
