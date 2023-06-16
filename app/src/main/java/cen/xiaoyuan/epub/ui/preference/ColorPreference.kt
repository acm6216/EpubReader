package cen.xiaoyuan.epub.ui.preference

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.utils.getIntCompat
import cen.xiaoyuan.epub.utils.putInt

/**
 * 自定义遇到问题无法解决，所以先这样用
 */
class ColorPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr, defStyleRes) {

    private lateinit var summaryView:TextView
    private lateinit var titleView:TextView
    private lateinit var src: ColorPreferenceView
    private var initUnit:(()->Unit)? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        titleView = holder.findViewById(R.id.color_title) as TextView
        summaryView = holder.findViewById(R.id.color_summary) as TextView
        src = holder.findViewById(R.id.color_widget) as ColorPreferenceView
        titleView.text = title.toString()
        val color = key.getIntCompat(0xff000000.toInt())
        summaryView.text = color.toHexColor()
        src.bgColor = color
        initUnit?.invoke()
    }

    fun initView(unit:(()->Unit)){
        initUnit = unit
    }

    fun setColorValue(value:Int,isSave:Boolean = false){
        if (isSave) {
            key.putInt(value)
            src.bgColor = value
            summaryView.text = value.toHexColor()
        }else{
            val color = key.getIntCompat(value)
            src.bgColor = color
            summaryView.text = color.toHexColor()
        }
    }

    private fun Int.toHexColor() = String.format("#%08X", 0xFFFFFFFF and this.toLong())

}