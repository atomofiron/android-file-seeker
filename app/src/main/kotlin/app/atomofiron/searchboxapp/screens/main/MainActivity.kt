package app.atomofiron.searchboxapp.screens.main

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.Insets
import androidx.core.view.WindowCompat.enableEdgeToEdge
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import app.atomofiron.common.util.Android
import app.atomofiron.common.util.extension.CornerPathDebug
import app.atomofiron.common.util.findBooleanByAttr
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.common.util.flow.collect
import app.atomofiron.common.util.flow.first
import app.atomofiron.common.util.hideKeyboard
import app.atomofiron.common.util.isDarkTheme
import app.atomofiron.common.util.reallyDisableFitsSystemWindows
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.ActivityMainBinding
import app.atomofiron.searchboxapp.custom.LayoutDelegate.getLayout
import app.atomofiron.searchboxapp.custom.LayoutDelegate.syncWithLayout
import app.atomofiron.searchboxapp.model.Layout.Ground
import app.atomofiron.searchboxapp.model.preference.AppOrientation
import app.atomofiron.searchboxapp.model.preference.AppTheme
import app.atomofiron.searchboxapp.screens.main.util.offerKeyCodeToChildren
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.setHapticEffect
import app.atomofiron.searchboxapp.utils.withAlpha
import com.google.android.material.color.DynamicColors
import lib.atomofiron.insets.InsetsSource
import lib.atomofiron.insets.builder
import lib.atomofiron.insets.insetsMargin
import lib.atomofiron.insets.insetsSource

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val rooFragment: Fragment get() = binding.navHostFragment.getFragment()

    private lateinit var viewState: MainViewState
    private lateinit var presenter: MainPresenter
    private var isFirstStart = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(window)
        val color = findColorByAttr(R.attr.colorBackground) withAlpha 1
        enableEdgeToEdge(navigationBarStyle = SystemBarStyle.auto(color, color))
        if (Android.Q) window.isNavigationBarContrastEnforced = false

        val viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.setView(this)
        presenter = viewModel.presenter
        viewState = viewModel.viewState
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = presenter.onBackButtonClick()
        })

        updateTheme(viewState.setTheme.value)
        onCreateView(savedInstanceState)
        // system insets providing breaks at least on Android 15 after app theme has been changed
        // enableEdgeToEdge() won’t help you in this hell
        // UPD 29.04.2025: WindowCompat.setDecorFitsSystemWindows() is not enough
        window.reallyDisableFitsSystemWindows()

        CornerPathDebug(resources.displayMetrics)
        if (Android.R) unlockHighFrameRate()
    }

    private fun onCreateView(savedInstanceState: Bundle?) {
        // todo Caused by: java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        presenter.onActivityCreate(this)

        binding.joystick.setOnClickListener { onEscClick() }
        binding.joystick.syncWithLayout(binding.root)

        if (savedInstanceState == null) onIntent(intent)

        val manager = getSystemService(Service.INPUT_METHOD_SERVICE) as InputMethodManager
        val onBackStackChangedListener: () -> Unit = {
            presenter.updateLightStatusBar(isDarkTheme())
            manager.hideSoftInputFromWindow(binding.root.windowToken, 0)
        }
        val childFragmentManager = supportFragmentManager.fragments.first().childFragmentManager
        childFragmentManager.addOnBackStackChangedListener(onBackStackChangedListener)
        supportFragmentManager.addOnBackStackChangedListener(onBackStackChangedListener)

        presenter.updateLightNavigationBar(isDarkTheme())
        presenter.updateLightStatusBar(isDarkTheme())
        onCollect()
        applyInsets()
    }

    override fun onStart() {
        super.onStart()
        when {
            isFirstStart -> isFirstStart = false
            else -> presenter.onMaximize()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onActivityDestroy()
    }

    private fun applyInsets() = binding.run {
        root.setInsetsModifier { _, windowInsets ->
            val navigation = windowInsets[ExtType.navigationBars]
            val tappable = windowInsets[ExtType.tappableElement]
            when (navigation.bottom) {
                0, tappable.bottom -> windowInsets
                else -> windowInsets.builder()
                    .set(ExtType.navigationBars, navigation.run { Insets.of(left, top, right, bottom / 2) })
                    .set(ExtType.tappableElement, tappable.run { Insets.of(left, top, right, bottom / 2) })
                    .build()
            }
        }
        joystick.insetsMargin()
        joystick.insetsSource { view ->
            if (!view.isVisible) {
                return@insetsSource InsetsSource.submit(ExtType.joystickBottom, Insets.NONE)
            }
            val parent = root.takeIf { it === view.parent }
                ?: throw IllegalStateException()
            val layout = parent.getLayout()
            when (layout.ground) {
                Ground.Bottom -> InsetsSource
                    .submit(ExtType.joystickBottom, Insets.of(0, 0, 0, parent.height - view.top - view.paddingTop))
                Ground.Left -> InsetsSource
                    .submit(ExtType.joystickTop, Insets.of(0, view.bottom, 0, 0))
                    .submit(ExtType.joystickFlank, Insets.of(view.right - view.paddingRight, 0, 0, 0))
                Ground.Right -> InsetsSource
                    .submit(ExtType.joystickTop, Insets.of(0, view.bottom, 0, 0))
                    .submit(ExtType.joystickFlank, Insets.of(0, 0, parent.width - view.left - view.paddingLeft, 0))
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        onIntent(intent)
    }

    private fun onIntent(intent: Intent?) {
        presenter.onIntent(intent ?: return)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when  {
            super.onKeyDown(keyCode, event) -> Unit
            keyCode == KeyEvent.KEYCODE_ESCAPE -> onEscClick()
            rooFragment.offerKeyCodeToChildren(keyCode) -> Unit
            else -> return false
        }
        return true
    }

    override fun setTheme(resId: Int) {
        super.setTheme(resId)
        if (::binding.isInitialized) {
            binding.root.setBackgroundColor(findColorByAttr(R.attr.colorBackground))
            binding.joystick.setComposition()
        }
    }

    private fun onCollect() {
        viewState.apply {
            setTheme.collect(lifecycleScope, ::updateTheme)
            setOrientation.collect(lifecycleScope, ::setOrientation)
            setJoystick.collect(lifecycleScope, binding.joystick::setComposition)
            hapticFeedback.first(lifecycleScope, binding.root::setHapticEffect)
        }
    }

    private fun updateTheme(theme: AppTheme) {
        when {
            theme is AppTheme.Light -> Unit
            theme.deepBlack == findBooleanByAttr(R.attr.isBlackDeep) -> Unit
            theme.deepBlack -> setTheme(R.style.CompatTheme_Amoled)
            else -> setTheme(R.style.CompatTheme)
        }
        presenter.onThemeApplied(isDarkTheme())
        // necessary to apply to the 'amoled' theme
        DynamicColors.applyToActivityIfAvailable(this)
    }

    private fun onEscClick() {
        val keyboardShown = binding.root.current[ExtType.ime].bottom > 0
        when {
            !keyboardShown -> presenter.onEscClick()
            else -> (binding.root.findFocus() as? EditText)?.hideKeyboard()
        }
    }

    private fun setOrientation(orientation: AppOrientation) {
        if (requestedOrientation != orientation.constant) {
            requestedOrientation = orientation.constant
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun unlockHighFrameRate() {
        window.attributes.preferredDisplayModeId = display.supportedModes
            .maxByOrNull { it.refreshRate }
            ?.modeId
            ?: return
        window.attributes = window.attributes
    }
}