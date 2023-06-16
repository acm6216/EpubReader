package cen.xiaoyuan.epub.ui.views

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatTextView
import cen.xiaoyuan.epub.R
class EnableCopyTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): AppCompatTextView(context, attrs, defStyleAttr) {

    var textActionMode:((String,Int)->Unit)?=null

    init {
        customSelectionActionModeCallback = create()
    }

    private fun processText():String{
        val selStart = selectionStart
        val selEnd = selectionEnd
        val min = 0.coerceAtLeast(selStart.coerceAtMost(selEnd))
        val max = 0.coerceAtLeast(selStart.coerceAtLeast(selEnd))
        return text.subSequence(min,max).toString()
    }

    fun create(): ActionMode.Callback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) api23() else api21()

    @RequiresApi(Build.VERSION_CODES.M)
    private fun api23(): ActionMode.Callback2 = object : ActionMode.Callback2() {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean = true

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            menu.clear()
            mode.menuInflater.inflate(R.menu.reader_text, menu)
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
            if(!item.isCheckable) textActionMode?.invoke(processText(),item.itemId)
            mode?.finish()
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {}
    }

    private fun api21(): ActionMode.Callback = object : ActionMode.Callback{
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean = true

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean{
            menu.clear()
            mode.menuInflater.inflate(R.menu.reader_text, menu)
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            if(!item.isCheckable) textActionMode?.invoke(processText(),item.itemId)
            mode.finish()
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {}
    }

}