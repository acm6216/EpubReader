package cen.xiaoyuan.epub.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.appcompat.widget.AppCompatImageView

class AutoImageView:AppCompatImageView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specSizeHeight = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(measureWidth(specSizeHeight).toInt(),specSizeHeight)
    }

    private fun measureWidth(specSize: Int):Float = specSize.toFloat()*5/7
}