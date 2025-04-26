package app.atomofiron.searchboxapp.custom

import android.view.Display
import android.view.Gravity
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView
import app.atomofiron.searchboxapp.custom.view.JoystickView
import app.atomofiron.searchboxapp.custom.view.dock.DockBarView
import app.atomofiron.searchboxapp.custom.view.dock.DockMode
import app.atomofiron.searchboxapp.custom.view.dock.DockNotch
import app.atomofiron.searchboxapp.model.Layout
import app.atomofiron.searchboxapp.model.ScreenSize
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.getDisplayCompat
import app.atomofiron.searchboxapp.utils.isRtl
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButtonToggleGroup
import lib.atomofiron.insets.InsetsCombining
import lib.atomofiron.insets.InsetsSource
import lib.atomofiron.insets.ViewInsetsDelegate
import lib.atomofiron.insets.findInsetsProvider
import lib.atomofiron.insets.insetsPadding
import lib.atomofiron.insets.insetsSource


object LayoutDelegate {

    operator fun invoke(
        root: ViewGroup,
        recyclerView: RecyclerView? = null,
        dockView: DockBarView? = null,
        tabLayout: MaterialButtonToggleGroup? = null,
        appBarLayout: AppBarLayout? = null,
        headerView: ExplorerHeaderView? = null,
        snackbarContainer: CoordinatorLayout? = null,
    ) {
        val insetsProvider = (root as View).findInsetsProvider()!!
        var layout = Layout(Layout.Ground.Bottom, withJoystick = true, root.isRtl())
        val dockDelegate = dockView?.insetsPadding(ExtType { barsWithCutout + joystickTop })
        val notch = root.resources.run {
            val size = getDimensionPixelSize(R.dimen.joystick_size) - 2 * getDimensionPixelSize(R.dimen.joystick_padding)
            DockNotch(size)
        }
        val recyclerDelegate = recyclerView?.insetsPadding(ExtType.invoke { barsWithCutout + ime + dock }, start = true, top = appBarLayout == null, end = true, bottom = true)
        root.setLayoutListener { new ->
            layout = new
            tabLayout?.isVisible = !layout.isWide
            dockView?.setMode(DockMode.Pinned(layout.ground, notch.takeIf { layout.run { ground.isBottom && withJoystick } }))
            dockDelegate?.applyDockLayout(layout)
            recyclerDelegate?.combining(if (layout.isBottom) null else InsetsCombining(ExtType.invoke { displayCutout + dock }) )
            /* нужно только когда есть табы
            explorerViews?.forEach {
                it.systemUiView.update(statusBar = landscape)
            }*/
            insetsProvider.requestInsets()
        }
        snackbarContainer?.insetsPadding(ExtType.invoke { barsWithCutout + ime + dock })
        appBarLayout?.insetsPadding(ExtType.invoke { barsWithCutout + dock + joystickFlank }, start = true, top = true, end = true)
        headerView?.insetsPadding(ExtType.invoke { barsWithCutout + dock }, start = true, top = true, end = true)
        dockView?.dockView?.insetsSource { view ->
            val insets = when {
                layout.isBottom -> Insets.of(0, 0, 0, view.height)
                layout.isLeft -> Insets.of(view.width, 0, 0, 0)
                layout.isRight -> Insets.of(0, 0, view.width, 0)
                else -> Insets.NONE
            }
            InsetsSource.submit(ExtType.dock, insets)
        }
    }

    private fun ViewInsetsDelegate.applyDockLayout(layout: Layout) {
        changeInsets {
            when {
                layout.isBottom -> padding(start, end, bottom)
                layout.isStart -> padding(start, top, bottom)
                else -> padding(top, end, bottom)
            }
        }
    }

    private var last = false
    private fun View.withJoystick(isBottom: Boolean): Boolean {
        val insets = ViewCompat.getRootWindowInsets(this)
        insets ?: return true
        val ime = insets.getInsets(Type.ime())
        if (isBottom && ime.bottom > 0) return last
        val tap = insets.getInsetsIgnoringVisibility(Type.tappableElement())
        val nav = insets.getInsetsIgnoringVisibility(Type.navigationBars())
        return when {
            nav.left > 0 -> false
            nav.right > 0 -> false
            nav.bottom == 0 -> true
            else -> nav.bottom != tap.bottom
        }.also { last = it }
    }

    private fun View.setLayoutListener(callback: (layout: Layout) -> Unit) {
        var layoutWas: Layout? = null
        val display = context.getDisplayCompat()
        addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            val layout = getLayout(display)
            if (layoutWas != layout) {
                layoutWas = layout
                callback(layout)
            }
        }
        callback(getLayout())
    }

    fun View.setScreenSizeListener(listener: (width: ScreenSize, height: ScreenSize) -> Unit) {
        var heightWas: ScreenSize? = null
        var verticalWas: ScreenSize? = null
        addOnLayoutChangeListener { view, left, top, right, bottom, _, _, _, _ ->
            val width = right - left
            val height = bottom - top
            val compactThreshold = view.resources.getDimensionPixelSize(R.dimen.screen_compact)
            val mediumThreshold = view.resources.getDimensionPixelSize(R.dimen.screen_medium)
            val horizontal = when {
                width < compactThreshold -> ScreenSize.Compact
                width < mediumThreshold -> ScreenSize.Medium
                else -> ScreenSize.Expanded
            }
            val vertical = when {
                height < compactThreshold -> ScreenSize.Compact
                height < mediumThreshold -> ScreenSize.Medium
                else -> ScreenSize.Expanded
            }
            when {
                horizontal == heightWas -> Unit
                vertical == verticalWas -> Unit
                else -> listener(horizontal, vertical)
            }
            heightWas = horizontal
            verticalWas = vertical
        }
    }

    fun View.getLayout(display: Display? = context.getDisplayCompat()): Layout {
        val width = if (width > 0) width else resources.displayMetrics.widthPixels
        val height = if (height > 0) height else resources.displayMetrics.heightPixels
        val maxSize = resources.getDimensionPixelSize(R.dimen.bottom_bar_max_width)
        val atTheBottom = width < height && width < maxSize
        val ground = when {
            atTheBottom -> Layout.Ground.Bottom
            display?.rotation == Surface.ROTATION_270 -> Layout.Ground.Left
            else -> Layout.Ground.Right
        }
        return Layout(ground, withJoystick(ground.isBottom), isRtl())
    }

    fun JoystickView.syncWithLayout(root: FrameLayout) {
        root.setLayoutListener { layout ->
            this.isVisible = layout.withJoystick
            updateLayoutParams<FrameLayout.LayoutParams> {
                val flags = when (layout.ground) {
                    Layout.Ground.Left -> Gravity.LEFT
                    Layout.Ground.Right -> Gravity.RIGHT
                    Layout.Ground.Bottom -> Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                }
                if ((gravity and flags) != flags) {
                    gravity = flags
                }
            }
        }
    }
}