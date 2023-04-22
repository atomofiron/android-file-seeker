package app.atomofiron.searchboxapp.custom

import android.annotation.SuppressLint
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.graphics.Insets
import androidx.core.view.*
import androidx.core.view.WindowInsetsCompat.Type
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.ExplorerHeaderView
import app.atomofiron.searchboxapp.custom.view.JoystickView
import app.atomofiron.searchboxapp.custom.view.SystemBarsBackgroundView
import app.atomofiron.searchboxapp.model.Layout
import app.atomofiron.searchboxapp.model.ScreenSize
import app.atomofiron.searchboxapp.utils.isLayoutRtl
import app.atomofiron.searchboxapp.utils.isRtl
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.navigation.NavigationView
import com.google.android.material.navigationrail.NavigationRailView
import lib.atomofiron.android_window_insets_compat.consumeInsets

@SuppressLint("PrivateResource")
class LayoutDelegate constructor(
    private val parent: ViewGroup,
    private val explorerViews: Array<ExplorerView>? = null,
    private val recyclerView: RecyclerView? = null,
    private val bottomView: BottomNavigationView? = null,
    private val railView: NavigationRailView? = null,
    private val systemUiView: SystemBarsBackgroundView? = null,
    private val tabLayout: MaterialButtonToggleGroup? = null,
    private val appbarLayout: AppBarLayout? = null,
    private val sideDock: NavigationView? = null,
    private val headerView: ExplorerHeaderView? = null,
    private val snackbarContainer: CoordinatorLayout? = null,
    private val joystickVisibilityCallback: ((Boolean) -> Unit)? = null,
) : OnApplyWindowInsetsListener {

    private val resources = parent.resources
    private val railSize = resources.getDimensionPixelSize(R.dimen.m3_navigation_rail_default_width)
    private val bottomSize = resources.getDimensionPixelSize(R.dimen.m3_bottom_nav_min_height)
    private val withBottomInset get() = layout.isBottom && (layout.withJoystick || bottomView != null)
    private val withFlankInset get() = !layout.isBottom && (layout.withJoystick || railView != null)
    private val currentBottomSize get() = if (withBottomInset) bottomSize else 0
    private val currentRailSize get() = if (withFlankInset) railSize else 0
    private var layout = Layout(Layout.Ground.Bottom, withJoystick = true)

    init {
        ViewCompat.setOnApplyWindowInsetsListener(parent, this)
        setLayout(layout)
        parent.setLayoutListener { layout ->
            if (this.layout == layout) return@setLayoutListener
            this.layout = layout
            parent.requestApplyInsets()
            setLayout(layout)
        }
        railView?.consumeInsets()
        sideDock?.consumeInsets()
    }

    override fun onApplyWindowInsets(parent: View, insets: WindowInsetsCompat): WindowInsetsCompat {
        joystickVisibilityCallback?.invoke(layout.withJoystick)
        headerView?.let {
            ViewCompat.dispatchApplyWindowInsets(it, insets.getHeaderViewInsets())
        }
        recyclerView?.let {
            ViewCompat.dispatchApplyWindowInsets(it, insets.getRecyclerViewInsets())
        }
        railView?.applyToRail(insets)
        sideDock?.applyToSideDock(insets)
        tabLayout?.let {
            ViewCompat.dispatchApplyWindowInsets(it, insets.getTabLayoutInsets())
        }
        systemUiView?.let {
            ViewCompat.dispatchApplyWindowInsets(it, insets)
        }
        bottomView?.let {
            ViewCompat.dispatchApplyWindowInsets(it, insets)
        }
        appbarLayout?.let {
            ViewCompat.dispatchApplyWindowInsets(it, insets)
        }
        explorerViews?.forEach {
            ViewCompat.dispatchApplyWindowInsets(it.headerView, insets.getHeaderViewInsets())
            ViewCompat.dispatchApplyWindowInsets(it.recyclerView, insets.getRecyclerViewInsets())
            ViewCompat.dispatchApplyWindowInsets(it.systemUiView, insets)
            it.onInsetsApplied()
        }
        snackbarContainer?.let {
            ViewCompat.dispatchApplyWindowInsets(it, insets.getRecyclerViewInsets())
        }
        parent.applyToAppbar(insets)
        return WindowInsetsCompat.CONSUMED
    }

    private fun setLayout(layout: Layout) {
        bottomView?.isVisible = !layout.isWide
        railView?.isVisible = layout.isWide
        tabLayout?.isVisible = !layout.isWide
        /* нужно только когда есть табы
        explorerViews?.forEach {
            it.systemUiView.update(statusBar = landscape)
        }*/
    }

    private fun WindowInsetsCompat.getRecyclerViewInsets(): WindowInsetsCompat {
        return custom {
            setInsets(insetsType, getInsets(insetsType).editForRecyclerView())
            setInsets(Type.ime(), getInsets(Type.ime()).editForRecyclerView())
        }
    }

    private fun WindowInsetsCompat.getTabLayoutInsets(): WindowInsetsCompat {
        return custom {
            setInsets(insetsType, getInsets(insetsType).editForTabLayout())
        }
    }

    private fun WindowInsetsCompat.getHeaderViewInsets(): WindowInsetsCompat {
        return custom {
            setInsets(insetsType, getInsets(insetsType).editForHeaderView())
        }
    }

    private inline fun WindowInsetsCompat.custom(action: WindowInsetsCompat.Builder.() -> Unit): WindowInsetsCompat {
        return WindowInsetsCompat.Builder(this).apply(action).build()
    }

    private fun Insets.editForRecyclerView(): Insets {
        val top = when {
            layout.isWide -> top
            tabLayout == null -> top
            else -> 0
        }
        val bottom = bottom + if (layout.isWide) 0 else currentBottomSize
        val left = left + if (layout.isLeft) currentRailSize else 0
        val right = right + if (layout.isRight) currentRailSize else 0
        return Insets.of(left, top, right, bottom)
    }

    private fun Insets.editForTabLayout(): Insets {
        val left = when {
            layout.isLeft -> left + currentRailSize
            else -> left
        }
        val right = when {
            layout.isRight -> right + currentRailSize
            else -> right
        }
        return Insets.of(left, top, right, bottom)
    }

    private fun Insets.editForHeaderView(): Insets {
        val left = when {
            layout.isLeft -> left + currentRailSize
            else -> left
        }
        val right = when {
            layout.isRight -> right + currentRailSize
            else -> right
        }
        val top = when {
            layout.isBottom -> if (tabLayout == null) top else 0
            else -> top
        }
        return Insets.of(left, top, right, bottom)
    }

    private fun View.applyToAppbar(insets: WindowInsetsCompat) {
        val appbarLayout: AppBarLayout? = findViewById(R.id.appbar_layout)
        appbarLayout ?: return
        ViewCompat.dispatchApplyWindowInsets(appbarLayout, insets)

        val collapsingLayout: CollapsingToolbarLayout? = appbarLayout.findViewById(R.id.collapsing_layout)
        collapsingLayout ?: return
        val custom = insets.getToolbarInsets()
        val defaultMargin = resources.getDimensionPixelSize(R.dimen.m3_appbar_expanded_title_margin_horizontal)
        collapsingLayout.run {
            val start = defaultMargin + if (resources.isRtl()) custom.right else custom.left
            val end = defaultMargin + if (resources.isRtl()) custom.left else custom.right
            if (expandedTitleMarginStart != start)
                expandedTitleMarginStart = start
            if (expandedTitleMarginEnd != end)
                expandedTitleMarginEnd = end
        }

        val toolbar: Toolbar? = collapsingLayout.findViewById(R.id.toolbar)
        toolbar ?: return
        toolbar.updatePadding(left = custom.left, right = custom.right)
    }

    private fun NavigationRailView.applyToRail(windowInsets: WindowInsetsCompat) {
        val insets = windowInsets.getInsets(insetsType)
        val topInset = if (layout.withJoystick) currentRailSize else 0
        val left = if (layout.isRight) 0 else insets.left
        val right = if (layout.isLeft) 0 else insets.right
        updatePadding(left, insets.top + topInset, right, insets.bottom)
    }

    private fun NavigationView.applyToSideDock(windowInsets: WindowInsetsCompat) {
        var gravity = (layoutParams as? DrawerLayout.LayoutParams)?.gravity
        gravity = when {
            gravity == Gravity.END && isLayoutRtl -> Gravity.LEFT
            gravity == Gravity.END -> Gravity.RIGHT
            gravity == Gravity.START && isLayoutRtl -> Gravity.RIGHT
            gravity == Gravity.START -> Gravity.LEFT
            else -> gravity
        }
        val insets = windowInsets.getInsets(insetsType)
        val currentRailSize = if (layout.withJoystick) currentRailSize else 0
        val left = when {
            gravity != Gravity.LEFT -> 0
            layout.isLeft -> insets.left + currentRailSize
            else -> insets.left
        }
        val right = when {
            gravity != Gravity.RIGHT -> 0
            layout.isRight -> insets.right + currentRailSize
            else -> insets.right
        }
        updateLayoutParams {
            val minWidth = resources.getDimensionPixelSize(R.dimen.design_navigation_max_width)
            width = minWidth + left + right
        }
        updatePadding(left, insets.top, right, insets.bottom)
    }

    private fun WindowInsetsCompat.getToolbarInsets(): Insets {
        val insets = getInsets(Type.displayCutout() or Type.navigationBars())
        if (!layout.withJoystick && railView == null && bottomView == null) {
            return insets
        }
        val left = if (layout.isLeft) currentRailSize else 0
        val right = if (layout.isRight) currentRailSize else 0
        val joystick = Insets.of(left, 0, right, 0)
        return Insets.add(insets, joystick)
    }

    companion object {
        private val insetsType = Type.systemBars() or Type.displayCutout()

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

        fun View.setLayoutListener(callback: (layout: Layout) -> Unit) {
            var layoutWas: Layout? = null
            addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                val layout = getLayout()
                if (layoutWas != layout) callback(layout)
                layoutWas = layout
            }
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

        fun View.getLayout(): Layout {
            val maxSize = resources.getDimensionPixelSize(R.dimen.bottom_bar_max_width)
            val atTheBottom = width < height && width < maxSize
            val ground = if (atTheBottom) Layout.Ground.Bottom else Layout.Ground.Right
            return Layout(ground, withJoystick(ground.isBottom))
        }

        fun JoystickView.syncOrientation(root: FrameLayout) {
            root.setLayoutListener { layout ->
                updateLayoutParams<FrameLayout.LayoutParams> {
                    val flags = when {
                        layout.isBottom -> Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                        else -> Gravity.END or Gravity.TOP
                    }
                    if ((gravity and flags) != flags) {
                        gravity = flags
                    }
                }
            }
        }
    }
}