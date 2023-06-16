package cen.xiaoyuan.epub.ui.views

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.R
import com.google.android.material.textfield.TextInputLayout

class ReadOnlyTextInputLayout @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr:Int = R.attr.textInputStyle
) :TextInputLayout(context,attr,defStyleAttr) {

    init {
        isExpandedHintEnabled = false
    }

}