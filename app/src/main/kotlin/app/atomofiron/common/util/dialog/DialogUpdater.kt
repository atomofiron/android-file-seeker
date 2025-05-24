package app.atomofiron.common.util.dialog

interface DialogUpdater {
    fun update(action: DialogConfig.() -> DialogConfig)
    fun showError(message: String?)
}
