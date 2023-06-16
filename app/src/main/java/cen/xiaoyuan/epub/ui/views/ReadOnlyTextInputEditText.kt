package cen.xiaoyuan.epub.ui.views

import android.content.Context
import android.os.Build
import android.text.method.MovementMethod
import android.util.AttributeSet
import android.view.View
import com.google.android.material.textfield.TextInputEditText

class ReadOnlyTextInputEditText @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr:Int = com.google.android.material.R.attr.editTextStyle
):TextInputEditText(context,attr,defStyleAttr) {

    init {
        setTextIsSelectable(isTextSelectable)
    }

    override fun getFreezesText(): Boolean = false

    override fun getDefaultEditable(): Boolean = false

    override fun getDefaultMovementMethod(): MovementMethod? = null

    override fun setTextIsSelectable(selectable: Boolean) {
        super.setTextIsSelectable(selectable)

        if(selectable){
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                focusable = View.FOCUSABLE_AUTO
            }
        }else {
            isClickable = false
            isFocusable = false
        }
    }
}