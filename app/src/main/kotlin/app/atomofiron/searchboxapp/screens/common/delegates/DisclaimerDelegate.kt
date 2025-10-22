package app.atomofiron.searchboxapp.screens.common.delegates

import androidx.core.graphics.Insets
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.DisclaimerBinding
import app.atomofiron.searchboxapp.custom.view.InsetsBackgroundView
import app.atomofiron.searchboxapp.custom.view.calcStatusBarPadding
import app.atomofiron.searchboxapp.screens.common.ActivityMode
import app.atomofiron.searchboxapp.utils.ExtType
import lib.atomofiron.insets.InsetsProvider
import lib.atomofiron.insets.InsetsSource
import lib.atomofiron.insets.insetsSource
import kotlin.math.max

fun DisclaimerBinding.apply(
    mode: ActivityMode,
    insetsBackground: InsetsBackgroundView,
    insetsProvider: InsetsProvider,
) {
    disclaimer.isVisible = !mode.default
    val stringId = when (mode) {
        is ActivityMode.Default -> return
        is ActivityMode.Share -> R.string.disclaimer_pick_files
        is ActivityMode.Receive -> R.string.disclaimer_choose_directory
    }
    disclaimer.setText(stringId)
    insetsProvider.addInsetsListener {
        val dock = it[ExtType.dock]
        val statusBar = it[ExtType.statusBars]
        val paddings = root.calcStatusBarPadding(it)
        disclaimer.translationY = (statusBar.top - paddings.bottom).toFloat()
        disclaimer.updatePadding(left = max(dock.left, paddings.left), right = max(dock.right, paddings.right))
    }
    insetsBackground += ExtType.topDisclaimer
    disclaimer.insetsSource {
        InsetsSource.submit(ExtType.topDisclaimer, Insets.of(0, it.height + it.y.toInt(), 0, 0))
    }
}
