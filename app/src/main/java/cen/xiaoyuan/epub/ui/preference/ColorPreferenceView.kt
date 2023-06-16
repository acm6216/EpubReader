package cen.xiaoyuan.epub.ui.preference

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class ColorPreferenceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var bgColor = 0xffffffff.toInt()
    set(value) {
        field = value
        invalidate()
    }

    private val stroke = 4f
    private val paint = Paint().apply {
        isAntiAlias = true
        strokeWidth = stroke
    }

    override fun draw(canvas: Canvas) {
        canvas.drawCircle(measuredWidth/2f,measuredHeight/2f,measuredWidth/2f-stroke*2,paint.apply {
            style = Paint.Style.FILL
            this.color = bgColor
        })
        canvas.drawCircle(measuredWidth/2f,measuredHeight/2f,measuredWidth/2f-stroke*2,paint.apply {
            style = Paint.Style.STROKE
            this.color = if(isDarkMode()) 0xffffffff.toInt() else 0xff000000.toInt()
        })
        super.draw(canvas)
    }

    private fun isDarkMode() = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

}