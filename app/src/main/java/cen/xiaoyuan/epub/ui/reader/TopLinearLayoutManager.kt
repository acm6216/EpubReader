package cen.xiaoyuan.epub.ui.reader

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TopLinearLayoutManager : LinearLayoutManager {

    constructor(
        context: Context,
        @RecyclerView.Orientation orientation:Int = RecyclerView.VERTICAL,
        reverseLayout:Boolean = false
    ):super(context, orientation, reverseLayout)

    constructor(context: Context):super(context)
    constructor(
        context:Context,
        attrs: AttributeSet,
        defStyleAttr:Int,
        defStyleRes:Int
    ):super(context, attrs, defStyleAttr, defStyleRes)

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State?,
        position: Int
    ) {
        val scroller = TopLinearSmoothScroller(recyclerView.context)
        scroller.targetPosition = position
        startSmoothScroll(scroller)
    }
}