package cen.xiaoyuan.epub.ui.views

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.appcompat.widget.AppCompatImageView

class CoverImageView: AppCompatImageView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int) : super(
        context, attrs, defStyleAttr)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val specSizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        setMeasuredDimension(specSizeWidth,measureHeight(specSizeWidth).toInt())
    }

    private fun measureHeight(specSize:Int):Float = specSize.toFloat()*7/5
}