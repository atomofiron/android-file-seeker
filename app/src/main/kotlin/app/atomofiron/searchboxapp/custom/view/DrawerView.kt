package app.atomofiron.searchboxapp.custom.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import app.atomofiron.common.util.DrawerStateListenerImpl
import com.google.android.material.navigation.NavigationView
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.databinding.LayoutDrawerNavigationBinding
import app.atomofiron.searchboxapp.utils.Const
import app.atomofiron.searchboxapp.utils.ExtType
import lib.atomofiron.insets.insetsPadding

class DrawerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.navigationViewStyle
) : NavigationView(context, attrs, defStyleAttr) {

    private val binding = LayoutDrawerNavigationBinding.inflate(LayoutInflater.from(context), this)
    private val titleInsetsDelegate = binding.drawerTitleContainer.insetsPadding(ExtType { barsWithCutout + joystickFlank }, start = true, top = true, end = true)
    private val rvInsetsDelegate = binding.drawerRv.insetsPadding(ExtType { barsWithCutout + joystickFlank })

    private val ibDockSide: ImageButton = findViewById(R.id.drawer_ib_dock_side)
    val recyclerView: RecyclerView = findViewById(R.id.drawer_rv)
    val isOpened: Boolean get() = drawerStateListener.isOpened

    var gravity: Int
        get() = (layoutParams as? DrawerLayout.LayoutParams)?.gravity ?: Gravity.NO_GRAVITY
        set(value) = updateGravity(value)

    private val drawerStateListener = DrawerStateListenerImpl()
    var onGravityChangeListener: ((gravity: Int) -> Unit)? = null

    init {
        ibDockSide.setOnClickListener {
            val gravity = if (gravity == Gravity.START) Gravity.END else Gravity.START
            onGravityChangeListener?.invoke(gravity)
        }
        val tvTitle = findViewById<TextView>(R.id.drawer_title)
        val styled = context.obtainStyledAttributes(attrs, R.styleable.VerticalDockView, defStyleAttr, 0)
        tvTitle.text = styled.getString(R.styleable.VerticalDockView_title)
        styled.recycle()

        binding.drawerTitleContainer.run {
            background = background.mutate()
            background.alpha = Const.ALPHA_80_PERCENT
        }
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams?) {
        super.setLayoutParams(params)

        val gravity = (params as? DrawerLayout.LayoutParams)?.gravity ?: Gravity.START
        val icDock = if (gravity == Gravity.START) R.drawable.ic_dock_end else R.drawable.ic_dock_start
        ibDockSide.setImageResource(icDock)
    }

    fun open() = (parent as DrawerLayout).openDrawer(gravity)

    fun close() = (parent as DrawerLayout).closeDrawer(gravity)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        (parent as DrawerLayout).addDrawerListener(drawerStateListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        (parent as DrawerLayout).removeDrawerListener(drawerStateListener)
    }

    private fun updateGravity(gravity: Int) {
        (layoutParams as DrawerLayout.LayoutParams).run {
            if (this.gravity == gravity) return
            this.gravity = gravity
        }
        layoutParams = layoutParams
        titleInsetsDelegate.changeInsets {
            when (gravity) {
                Gravity.START -> padding(start, top)
                Gravity.END -> padding(top, end)
            }
        }
        rvInsetsDelegate.changeInsets {
            when (gravity) {
                Gravity.START -> padding(start, top, bottom)
                Gravity.END -> padding(top, end, bottom)
            }
        }
        onSizeChanged(width, height, width, height) // trigger maybeClearCornerSizeAnimationForDrawerLayout()
    }
}