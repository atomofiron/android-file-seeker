package app.atomofiron.searchboxapp.screens.curtain

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.Insets
import androidx.core.view.doOnNextLayout
import androidx.core.view.marginBottom
import androidx.core.view.marginTop
import androidx.fragment.app.DialogFragment
import app.atomofiron.common.arch.BaseFragment
import app.atomofiron.common.arch.BaseFragmentImpl
import app.atomofiron.common.arch.TranslucentFragment
import app.atomofiron.common.util.Alert
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.MaterialDimen
import app.atomofiron.common.util.flow.viewCollect
import app.atomofiron.common.util.isDarkTheme
import app.atomofiron.fileseeker.BuildConfig
import app.atomofiron.fileseeker.R
import app.atomofiron.fileseeker.databinding.FragmentCurtainBinding
import app.atomofiron.searchboxapp.model.other.LabeledAction
import app.atomofiron.searchboxapp.screens.curtain.fragment.BottomSheetKeyboardBehavior
import app.atomofiron.searchboxapp.screens.curtain.fragment.CurtainContentDelegate
import app.atomofiron.searchboxapp.screens.curtain.fragment.CurtainNode
import app.atomofiron.searchboxapp.screens.curtain.fragment.TransitionAnimator
import app.atomofiron.searchboxapp.screens.curtain.model.CurtainAction
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainApi
import app.atomofiron.searchboxapp.screens.curtain.util.CurtainBackground
import app.atomofiron.searchboxapp.utils.Alpha
import app.atomofiron.searchboxapp.utils.ExtType
import app.atomofiron.searchboxapp.utils.colorAttr
import app.atomofiron.searchboxapp.utils.context
import app.atomofiron.searchboxapp.utils.getColorByAttr
import app.atomofiron.searchboxapp.utils.makeSnackbar
import app.atomofiron.searchboxapp.utils.withAlpha
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.color.MaterialColors
import lib.atomofiron.insets.ExtendedWindowInsets
import lib.atomofiron.insets.insetsPadding
import java.lang.ref.WeakReference
import kotlin.math.max

private const val SAVED_STACK = "SAVED_STACK"
private const val MAX_OVERLAY_SATURATION = 200

class CurtainFragment : DialogFragment(R.layout.fragment_curtain),
    BaseFragment<CurtainFragment, CurtainViewState, CurtainPresenter, FragmentCurtainBinding> by BaseFragmentImpl(),
    TranslucentFragment
{
    private lateinit var binding: FragmentCurtainBinding
    private lateinit var behavior: BottomSheetKeyboardBehavior<View>
    private val stack: MutableList<CurtainNode> = ArrayList()
    private lateinit var contentDelegate: CurtainContentDelegate
    private lateinit var transitionAnimator: TransitionAnimator
    private var snackbarView = WeakReference<View>(null)
    private var overlayColor = 0

    override val isLightStatusBar: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViewModel(this, CurtainViewModel::class, savedInstanceState)
        overlayColor = requireContext().getColorByAttr(R.attr.colorOverlay)

        when (savedInstanceState) {
            null -> stack.add(CurtainNode(viewState.initialLayoutId, view = null, isCancelable = true))
            else -> savedInstanceState.getIntArray(SAVED_STACK)?.let { ids ->
                val restored = ids.map { CurtainNode(layoutId = it, view = null, isCancelable = true) }
                stack.addAll(restored)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentCurtainBinding.bind(view).apply {
            curtainParent.elevation = resources.getDimension(MaterialDimen.m3_comp_snackbar_container_elevation).inc()
            curtainSheet.clipToOutline = true
            curtainSheet.background = CurtainBackground(requireContext())
            curtainSheet.setOnClickListener { /* intercept clicks */ }
            root.setOnClickListener {
                root.setOnClickListener(null)
                root.setOnLongClickListener(null)
                root.isClickable = false
                root.isLongClickable = false
                tryHide()
            }
            root.setOnLongClickListener {
                if (BuildConfig.DEBUG) showTestSnackbar()
                true
            }
            root.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> updateSnackbarTranslation() }
            val layoutParams = curtainSheet.layoutParams as CoordinatorLayout.LayoutParams
            behavior = layoutParams.behavior as BottomSheetKeyboardBehavior
            behavior.addBottomSheetCallback(BottomSheetCallbackImpl(curtainSheet))
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
            behavior.setWindow(requireActivity().window)

            onApplyInsets()
        }
        transitionAnimator = TransitionAnimator(binding, ::updateSnackbarTranslation)
        viewState.onViewCollect()
    }

    private fun showTestSnackbar() = showSnackbar(Alert("Boo", error = snackbarView.get() != null, important = true), LabeledAction(R.string.dismiss))

    override fun FragmentCurtainBinding.onApplyInsets() {
        root.insetsPadding(ExtType { barsWithCutout + joystickFlank }, start = true, top = true, end = true)
        val verticalPadding = resources.getDimensionPixelSize(R.dimen.curtain_padding)
        root.setInsetsModifier { _, windowInsets ->
            val bars = windowInsets[ExtType.barsWithCutout].bottom
            val joystick = windowInsets[ExtType.joystickBottom].bottom
            val bottomPadding = max(joystick, bars + verticalPadding)
            behavior.onApplyWindowInsets(windowInsets, bottomPadding)
            ExtendedWindowInsets.Builder()
                .set(ExtType.curtain, Insets.of(0, verticalPadding, 0, bottomPadding))
                .build()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray(SAVED_STACK, stack.map { it.layoutId }.toIntArray())
    }

    override fun onBack(soft: Boolean): Boolean {
        when {
            transitionAnimator.transitionIsRunning -> Unit
            !viewState.cancelable.value -> Unit
            !contentDelegate.showPrev() -> hide()
        }
        return true
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            presenter.onShown()
        }
    }

    private fun tryHide() {
        if (viewState.cancelable.value) {
            hide()
        }
    }

    override fun CurtainViewState.onViewCollect() {
        viewCollect(adapter, collector = ::onAdapterCollect)
        viewCollect(cancelable, collector = behavior::setHideable)
        viewCollect(action, collector = ::onActionCollect)
    }

    private fun onAdapterCollect(adapter: CurtainApi.Adapter<*>) {
        contentDelegate = CurtainContentDelegate(binding, stack, adapter, transitionAnimator, presenter)
        contentDelegate.showLast()
        expand()
    }

    private fun onActionCollect(action: CurtainAction) {
        when (action) {
            is CurtainAction.ShowNext -> contentDelegate.showNext(action.layoutId)
            is CurtainAction.ShowPrev -> contentDelegate.showPrev()
            is CurtainAction.Hide -> hide(action.irrevocably)
            is CurtainAction.ShowSnackbar -> showSnackbar(action.alert)
        }
    }

    private fun showSnackbar(alert: Alert.Uni, action: LabeledAction? = null) {
        val context = binding.context
        val snackbar = binding.root.makeSnackbar(alert, action)
        if (!context.isDarkTheme()) {
            val background = context.colorAttr(if (alert.error) MaterialAttr.colorErrorContainer else MaterialAttr.colorSurface)
            val text = context.colorAttr(if (alert.error) MaterialAttr.colorOnErrorContainer else MaterialAttr.colorOnSurface)
            val action = context.colorAttr(if (alert.error) MaterialAttr.colorError else MaterialAttr.colorPrimaryDark)
            val alpha = ResourcesCompat.getFloat(resources, MaterialDimen.mtrl_snackbar_background_overlay_color_alpha)
            val backgroundTint = MaterialColors.layer(text, background, alpha)
            snackbar
                .setBackgroundTint(backgroundTint)
                .setTextColor(text)
                .setActionTextColor(action)
        }
        snackbarView = WeakReference(snackbar.view)
        snackbar.view.doOnNextLayout { updateSnackbarTranslation() }
        snackbar.show()
    }

    private fun updateSaturation() {
        val sheet = binding.curtainSheet
        val parent = sheet.parent as View
        val alpha = Alpha(1f - (sheet.bottom - parent.height) / sheet.height.toFloat())
        val overlayAlpha = (MAX_OVERLAY_SATURATION * alpha).toInt()
        binding.root.setBackgroundColor(overlayColor withAlpha overlayAlpha)
    }

    private fun updateSnackbarTranslation() {
        val snackbarView = snackbarView.get() ?: return
        val params = snackbarView.layoutParams as ViewGroup.MarginLayoutParams
        val minBottom = binding.root.paddingTop + params.topMargin + snackbarView.height
        val minOffset = minBottom - binding.root.height.toFloat()
        val bottomInset = params.bottomMargin - params.topMargin
        val sheet = binding.curtainSheet
        val parent = sheet.parent as View
        var offset = snackbarView.run { height + marginBottom + marginTop }.toFloat()
        offset *= Alpha((sheet.bottom - parent.height) / sheet.height.toFloat())
        offset += binding.curtainSheet.top - binding.curtainSheet.height + bottomInset
        snackbarView.translationY = max(minOffset, offset)
    }

    private fun expand() {
        view?.post {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    private fun hide(irrevocably: Boolean = false) {
        if (irrevocably) behavior.isDraggable = false
        behavior.isHideable = true
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    private inner class BottomSheetCallbackImpl(bottomSheet: View) : BottomSheetBehavior.BottomSheetCallback() {
        init {
            bottomSheet.post {
                updateSaturation()
                updateSnackbarTranslation()
            }
        }

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            updateSaturation()
            updateSnackbarTranslation()
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                presenter.onHidden()
            }
        }

        // slideOffset is broken
        override fun onSlide(bottomSheet: View, slideOffset: Float) {
            updateSaturation()
            updateSnackbarTranslation()
        }
    }
}