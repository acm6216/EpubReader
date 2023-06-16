package cen.xiaoyuan.epub.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updatePadding
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.utils.displayWidth
import cen.xiaoyuan.epub.utils.getDimension
import cen.xiaoyuan.epub.utils.getDimensionPixelSize

@SuppressLint("PrivateResource")
class WidthLimitedConstraintLayout @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr:Int = 0
): ConstraintLayout(context,attr,defStyleAttr) {

    @SuppressLint("PrivateResource")
    private val verticalPadding =
        context.getDimensionPixelSize(com.google.android.material.R.dimen.design_navigation_padding_bottom)
    private val limitMaxWidth = getContext().getDimensionPixelSize(R.dimen.navigation_max_width)

    init {
        updatePadding(top = verticalPadding, bottom = verticalPadding)
        elevation = context.getDimension(com.google.android.material.R.dimen.design_navigation_elevation)
        fitsSystemWindows = true
        setWillNotDraw(false)
    }

    override fun onMeasure(widSpec: Int, heightSpec: Int) {
        var widthSpec = widSpec
        var maxWidth = (context.displayWidth).coerceIn(0..limitMaxWidth)
        when (MeasureSpec.getMode(widthSpec)) {
            MeasureSpec.AT_MOST -> {
                maxWidth = maxWidth.coerceAtMost(MeasureSpec.getSize(widthSpec))
                widthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY)
            }
            MeasureSpec.UNSPECIFIED ->
                widthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY)
            MeasureSpec.EXACTLY -> {}
        }
        super.onMeasure(widthSpec, heightSpec)
    }
}