package app.atomofiron.searchboxapp.screens.viewer.recycler

import android.text.Spannable
import android.text.SpannableString
import android.widget.TextView
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.searchboxapp.R
import app.atomofiron.searchboxapp.custom.view.style.EntireLineSpan
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.model.textviewer.TextLineMatch
import app.atomofiron.searchboxapp.custom.view.style.RoundedBackgroundSpan
import com.google.android.material.R as MaterialR

class TextViewerHolder(private val textView: TextView) : GeneralHolder<TextLine>(textView) {
    private val spanPart: RoundedBackgroundSpan
        get() = RoundedBackgroundSpan(
            backgroundColor = context.findColorByAttr(MaterialR.attr.colorSurfaceVariant),
            borderColor = context.findColorByAttr(MaterialR.attr.colorSecondary),
            textColor = context.findColorByAttr(MaterialR.attr.colorOnSurfaceVariant),
            context.resources.getDimension(R.dimen.background_span_corner_radius),
            context.resources.getDimension(R.dimen.background_span_border_thickness),
    )

    private val spanPartFocus: RoundedBackgroundSpan
        get() = RoundedBackgroundSpan(
            backgroundColor = context.findColorByAttr(MaterialR.attr.colorSecondary),
            borderColor = context.findColorByAttr(MaterialR.attr.colorPrimary),
            textColor = context.findColorByAttr(MaterialR.attr.colorOnSecondary),
            context.resources.getDimension(R.dimen.background_span_corner_radius),
            context.resources.getDimension(R.dimen.background_span_border_thickness),
    )

    private val spanLine: EntireLineSpan
        get() = EntireLineSpan(
            context.findColorByAttr(MaterialR.attr.colorSecondary),
            context.findColorByAttr(MaterialR.attr.colorOnSecondary),
            context.resources.getDimension(R.dimen.background_span_corner_radius)
    )

    private val spanLineFocus: EntireLineSpan
        get() = EntireLineSpan(
            context.findColorByAttr(MaterialR.attr.colorTertiary),
            context.findColorByAttr(MaterialR.attr.colorOnTertiary),
            context.resources.getDimension(R.dimen.background_span_corner_radius)
    )

    override fun onBind(item: TextLine, position: Int) = Unit

    fun onBind(item: TextLine, matches: List<TextLineMatch>?, indexFocus: Int?) {
        when {
            matches.isNullOrEmpty() -> textView.text = item.text
            else -> {
                val spannable = SpannableString(item.text)
                matches.forEachIndexed { index, match ->
                    val byteOffset = match.byteOffset - item.byteOffset
                    val start = item.text.toByteArray().slice(0 until byteOffset.toInt()).toByteArray().toString(charset = Charsets.UTF_8).length
                    val end = start + match.length
                    val forTheEntireLine = start == 0 && end == item.text.length
                    val span: Any = when {
                        forTheEntireLine && index == indexFocus -> spanLineFocus
                        forTheEntireLine -> spanLine
                        index == indexFocus -> spanPartFocus
                        else -> spanPart
                    }
                    spannable.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                textView.text = spannable
            }
        }
    }
}