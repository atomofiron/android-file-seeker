package app.atomofiron.common.util.dialog

import app.atomofiron.searchboxapp.model.other.UniText

interface DialogUpdater {
    fun update(action: DialogConfig.() -> DialogConfig)
    fun showError(message: UniText?)
}
