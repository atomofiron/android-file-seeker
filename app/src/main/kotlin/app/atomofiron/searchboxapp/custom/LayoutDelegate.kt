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
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.drawable.colorSurfaceContainer
import app.atomofiron.searchboxapp.custom.view.JoystickView
import app.atomofiron.searchboxapp.custom.view.dock.DockBarView
import app.atomofiron.searchboxapp.custom.view.dock.DockMode
import app.atomofiron.searchboxapp.custom.view.dock.shape.DockNotch
import app.atomofiron.searchboxapp.custom.view.dock.shape.DockStyle
import app.atomofiron.searchboxapp.custom.view.layout.MeasureProvider
import app.atomofiron.searchboxapp.custom.view.layout.RootFrameLayout
import app.atomofiron.searchboxapp.model.Layout
import app.atomofiron.searchboxapp.model.ScreenSize
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.addOnAttachListener
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

    fun MeasureProvider.apply(
        recyclerView: RecyclerView? = null,
        dockView: DockBarView? = null,
        tabLayout: MaterialButtonToggleGroup? = null,
        appBarLayout: AppBarLayout? = null,
        snackbarContainer: CoordinatorLayout? = null,
    ) {
        val insetsProvider = (view as View).findInsetsProvider()!!
        var layoutWas: Layout? = null
        val dockDelegate = dockView?.insetsPadding(ExtType { barsWithCutout + joystickTop })
        val notch = view.resources.run {
            val size = getDimensionPixelSize(R.dimen.joystick_size) - 2 * getDimensionPixelSize(R.dimen.joystick_padding)
            DockNotch(size)
        }
        val recyclerDelegate = recyclerView?.insetsPadding(ExtType.invoke { barsWithCutout + ime + dock + joystick }, start = true, top = appBarLayout == null, end = true, bottom = true)
        addLayoutListener { layout ->
            if (layout == layoutWas) {
                return@addLayoutListener
            }
            layoutWas = layout
            val tappableBottom = insetsProvider.current[ExtType.tappableElement].bottom > 0
            tabLayout?.isVisible = !layout.isWide
            dockView?.apply(layout, notch, dockDelegate!!, tappableBottom)
            recyclerDelegate?.combining(if (layout.isBottom) null else InsetsCombining(ExtType.invoke { displayCutout + dock }) )
            /* нужно только когда есть табы
            explorerViews?.forEach {
                it.systemUiView.update(statusBar = landscape)
            }*/
            insetsProvider.requestInsets()
        }
        snackbarContainer?.insetsPadding(ExtType.invoke { barsWithCutout + ime + dock })
        appBarLayout?.insetsPadding(ExtType.invoke { barsWithCutout + dock + joystickFlank }, start = true, top = true, end = true)
        dockView?.dockView?.insetsSource { view ->
            val layout = layoutWas
            val insets = when {
                layout == null -> Insets.NONE
                layout.isBottom -> Insets.of(0, 0, 0, view.height)
                layout.isLeft -> Insets.of(view.width, 0, 0, 0)
                layout.isRight -> Insets.of(0, 0, view.width, 0)
                else -> Insets.NONE
            }
            InsetsSource.submit(ExtType.dock, insets)
        }
    }

    private fun DockBarView.apply(layout: Layout, notch: DockNotch, delegate: ViewInsetsDelegate, tappableBottom: Boolean) {
        delegate.changeInsets {
            when {
                !tappableBottom && !layout.isBottom -> when {
                    layout.isStart -> padding(start, top)
                    layout.isEnd -> padding(top, end)
                }
                layout.isBottom -> padding(start, end, bottom)
                layout.isStart -> padding(start, top, bottom)
                layout.isEnd -> padding(top, end, bottom)
            }
        }
        val translucent = !layout.ground.isBottom
        val fill = when {
            translucent -> context.findColorByAttr(R.attr.colorBackground)
            else -> context.colorSurfaceContainer()
        }
        setStyle(DockStyle(fill = fill, translucent = translucent))
        setMode(DockMode.Pinned(layout.ground, notch.takeIf { layout.run { ground.isBottom && withJoystick } }))
    }

    private fun View.withJoystick(): Boolean {
        val insets = ViewCompat.getRootWindowInsets(this)
        insets ?: return false
        val tap = insets.getInsetsIgnoringVisibility(Type.tappableElement())
        val nav = insets.getInsetsIgnoringVisibility(Type.navigationBars())
        return when {
            nav.left > 0 -> false
            nav.right > 0 -> false
            nav.bottom == 0 -> true
            else -> nav.bottom != tap.bottom
        }
    }

    fun MeasureProvider.addLayoutListener(callback: (Layout) -> Unit) {
        val display = view.context.getDisplayCompat()
        var was: Layout? = null
        val action = {
            val layout = view.getLayout(display)
            if (layout != was) {
                was = layout
                callback(layout)
            }
        }
        view.addOnAttachListener(oneTime = true, onAttach = action)
        addMeasureListener { _, _ -> action() }
    }

    fun View.setScreenSizeListener(listener: (width: ScreenSize, height: ScreenSize) -> Unit) {
        var heightWas: ScreenSize? = null
        var verticalWas: ScreenSize? = null
        val compactThreshold = resources.getDimensionPixelSize(R.dimen.screen_compact)
        val mediumThreshold = resources.getDimensionPixelSize(R.dimen.screen_medium)
        addOnLayoutChangeListener { view, left, top, right, bottom, _, _, _, _ ->
            val width = right - left
            val height = bottom - top
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

    fun ViewGroup.getLayout(): Layout = getLayout(context.getDisplayCompat())

    fun ViewGroup.getLayout(display: Display?): Layout {
        val metrics = resources.displayMetrics // size should be the same for each place in the view tree
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val maxSpace = resources.getDimensionPixelSize(R.dimen.bottom_bar_max_width)
        val minSpace = resources.getDimensionPixelSize(R.dimen.min_space_with_joystick)
        val ground = when {
            width < height && width < maxSpace -> Layout.Ground.Bottom
            display?.rotation == Surface.ROTATION_270 -> Layout.Ground.Left
            else -> Layout.Ground.Right
        }
        val largeScreen = width >= maxSpace && height >= maxSpace
        val smallScreen = width < minSpace && height < minSpace
        val withJoystick = !smallScreen && (largeScreen || withJoystick())
        return Layout(ground, withJoystick, isRtl())
    }

    fun JoystickView.syncWithLayout(root: RootFrameLayout) {
        var was: Layout? = null
        root.addLayoutListener { layout ->
            if (layout == was) {
                return@addLayoutListener
            }
            was = layout
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