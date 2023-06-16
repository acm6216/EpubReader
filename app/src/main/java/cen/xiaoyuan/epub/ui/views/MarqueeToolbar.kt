package cen.xiaoyuan.epub.ui.views

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.TextView
import androidx.appcompat.R
import androidx.appcompat.widget.Toolbar

class MarqueeToolbar @JvmOverloads constructor(
    context: Context,
    attr: AttributeSet? = null,
    defStyleAttr:Int = R.attr.toolbarStyle
): Toolbar(context,attr,defStyleAttr) {

    override fun setTitle(resId: Int) {
        ensureTitleTarget()
        super.setTitle(resId)
    }

    override fun setTitle(title: CharSequence?) {
        ensureTitleTarget()
        super.setTitle(title)
    }

    private fun ensureTitleTarget(){
        val titleTextView = try {
            Toolbar::class.java.getDeclaredField("mTitleTextView")
                .apply { isAccessible = true }
                .get(this) as TextView?
        }catch (e:Exception){
            e.printStackTrace()
            null
        }?:return
        titleTextView.ellipsize = TextUtils.TruncateAt.MARQUEE
        titleTextView.isSelected = true
    }

}