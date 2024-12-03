package app.atomofiron.searchboxapp.utils

import android.app.Activity
import android.graphics.*
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import app.atomofiron.searchboxapp.R

class FilterActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this)
        setContentView(root)

        root.orientation = LinearLayout.VERTICAL
        root.gravity = Gravity.CENTER_VERTICAL
        root.setBackgroundColor(Color.GRAY)
        val size = Math.min(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels) / 3
        val textPaint = Paint().apply { color = Color.BLACK; textSize = 36f; isFakeBoldText = true }
        val maskPaint = Paint()

        var line = LinearLayout(this)
        for (i in 0 until 18) {
            if (i % 3 == 0) {
                line = LinearLayout(this)
                line.orientation = LinearLayout.HORIZONTAL
                line.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                root.addView(line)
            }
            val dst = ContextCompat.getDrawable(this, R.drawable.ic_record_blue_24dp)!!
            val ids = arrayOf(
                    R.drawable.ic_record_red_24dp,
                    R.drawable.ic_record_cian_24dp,
                    R.drawable.ic_record_black_24dp,
                    R.drawable.ic_record_white_24dp)
            val src = Array(4) { ContextCompat.getDrawable(this@FilterActivity, ids[it])!! }
            dst.setBounds(0, 0, size, size)
            src.forEach { it.setBounds(0, 0, size, size); it.alpha = 80 }
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val mask = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            val canvasMask = Canvas(mask)
            dst.draw(canvas)
            src.forEach { it.draw(canvasMask) }
            maskPaint.xfermode = PorterDuffXfermode(intToMode(i))
            canvas.drawBitmap(mask, 0f, 0f, maskPaint)
            canvas.drawText(intToString(i), 6f, 32f, textPaint)

            val view = ImageView(this)
            view.setImageBitmap(bitmap)
            line.addView(view)
        }
    }

    private fun intToMode(value: Int): PorterDuff.Mode {
        return when (value) {
            0 -> PorterDuff.Mode.CLEAR
            1 -> PorterDuff.Mode.SRC
            2 -> PorterDuff.Mode.DST
            3 -> PorterDuff.Mode.SRC_OVER
            4 -> PorterDuff.Mode.DST_OVER
            5 -> PorterDuff.Mode.SRC_IN
            6 -> PorterDuff.Mode.DST_IN
            7 -> PorterDuff.Mode.SRC_OUT
            8 -> PorterDuff.Mode.DST_OUT
            9 -> PorterDuff.Mode.SRC_ATOP
            10 -> PorterDuff.Mode.DST_ATOP
            11 -> PorterDuff.Mode.XOR
            12 -> PorterDuff.Mode.ADD
            13 -> PorterDuff.Mode.MULTIPLY
            14 -> PorterDuff.Mode.SCREEN
            15 -> PorterDuff.Mode.OVERLAY
            16 -> PorterDuff.Mode.DARKEN
            17 -> PorterDuff.Mode.LIGHTEN
            else -> PorterDuff.Mode.CLEAR
        }
    }

    private fun intToString(value: Int): String {
        return when (value) {
            0 -> "CLEAR"
            1 -> "SRC"
            2 -> "DST"
            3 -> "SRC_OVER"
            4 -> "DST_OVER"
            5 -> "SRC_IN"
            6 -> "DST_IN"
            7 -> "SRC_OUT"
            8 -> "DST_OUT"
            9 -> "SRC_ATOP"
            10 -> "DST_ATOP"
            11 -> "XOR"
            12 -> "ADD"
            13 -> "MULTIPLY"
            14 -> "SCREEN"
            15 -> "OVERLAY"
            16 -> "DARKEN"
            17 -> "LIGHTEN"
            else -> "null"
        }
    }
}