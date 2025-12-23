package app.atomofiron.searchboxapp.screens.viewer.recycler

import android.text.Spannable
import android.text.SpannableString
import android.widget.TextView
import app.atomofiron.common.recycler.GeneralHolder
import app.atomofiron.common.util.MaterialAttr
import app.atomofiron.common.util.extension.debugFail
import app.atomofiron.common.util.findColorByAttr
import app.atomofiron.fileseeker.R
import app.atomofiron.searchboxapp.custom.view.style.EntireLineSpan
import app.atomofiron.searchboxapp.custom.view.style.RoundedBackgroundSpan
import app.atomofiron.searchboxapp.model.textviewer.MatchList
import app.atomofiron.searchboxapp.model.textviewer.TextLine
import app.atomofiron.searchboxapp.utils.countChars

class TextViewerHolder(private val textView: TextView) : GeneralHolder<TextLine>(textView) {

    private val charset = Charsets.UTF_8

    private val spanPart: RoundedBackgroundSpan
        get() = RoundedBackgroundSpan(
            backgroundColor = context.findColorByAttr(MaterialAttr.colorSurfaceVariant),
            borderColor = context.findColorByAttr(MaterialAttr.colorSecondary),
            textColor = context.findColorByAttr(MaterialAttr.colorOnSurfaceVariant),
            context.resources.getDimension(R.dimen.background_span_corner_radius),
            context.resources.getDimension(R.dimen.background_span_border_thickness),
    )

    private val spanPartFocus: RoundedBackgroundSpan
        get() = RoundedBackgroundSpan(
            backgroundColor = context.findColorByAttr(MaterialAttr.colorSecondary),
            borderColor = context.findColorByAttr(MaterialAttr.colorSecondary),
            textColor = context.findColorByAttr(MaterialAttr.colorOnSecondary),
            context.resources.getDimension(R.dimen.background_span_corner_radius),
            context.resources.getDimension(R.dimen.background_span_border_thickness),
    )

    private val spanLine: EntireLineSpan
        get() = EntireLineSpan(
            context.findColorByAttr(MaterialAttr.colorSecondary),
            context.findColorByAttr(MaterialAttr.colorOnSecondary),
            context.resources.getDimension(R.dimen.background_span_corner_radius)
    )

    private val spanLineFocus: EntireLineSpan
        get() = EntireLineSpan(
            context.findColorByAttr(MaterialAttr.colorTertiary),
            context.findColorByAttr(MaterialAttr.colorOnTertiary),
            context.resources.getDimension(R.dimen.background_span_corner_radius)
    )

    override fun onBind(item: TextLine, position: Int) {
        textView.text = item.text.decode()
        // android:textIsSelectable="true" breaks down
        textView.setTextIsSelectable(true)
    }

    fun bindMatches(item: TextLine, position: Int, matches: MatchList, indexFocus: Int) {
        truePosition = position
        val spannable = SpannableString(item.text.decode())
        matches.forEachIndexed { index, match ->
            val bytesStart = match.offset.toInt() - item.offset
            val bytesEnd = (bytesStart + match.length.toInt())
            val start = item.text.countChars(charset, 0..<bytesStart)
            if (bytesStart < 0 || bytesEnd < 0) {
                debugFail { "$bytesStart < 0 || $bytesEnd < 0, text ${item.text.decode()}" }
                return bind(item, position)
            }
            val length = item.text.countChars(charset, bytesStart..<bytesEnd)
            val end = start + length
            val forTheEntireLine = start == 0 && end == item.length
            val span: Any = when {
                forTheEntireLine && index == indexFocus -> spanLineFocus
                forTheEntireLine -> spanLine
                index == indexFocus -> spanPartFocus
                else -> spanPart
            }
            spannable.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        textView.text = spannable
        // android:textIsSelectable="true" breaks down
        textView.setTextIsSelectable(true)
    }

    private fun ByteArray.decode() = String(this, charset)
}