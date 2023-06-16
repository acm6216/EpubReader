package cen.xiaoyuan.epub.ui.views

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.TranslateAnimation
import androidx.annotation.AttrRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class OverScrollLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    defStyleRes: Int = 0
): ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    companion object{
        private const val ANIM_TIME = 400L
        private const val DAMPING_COEFFICIENT = 0.3f
    }

    private val original = Rect()
    private var isMoved = false
    private var isSuccess = false
    private var startYpos:Float = 0f
    private var childView:RecyclerView?=null

    var onScrollListener:((Int)->Unit)?=null

    override fun onFinishInflate() {
        super.onFinishInflate()
        children.find { it is RecyclerView }?.also {
            childView = it as RecyclerView
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        childView?.also {
            original.set(it.left, it.top, it.right, it.bottom)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        ev?:return super.dispatchTouchEvent(ev)
        val touchYpos = ev.y
        if (touchYpos >= original.bottom || touchYpos <= original.top) {
            if (isMoved) {
                recoverLayout()
            }
            return true
        }
        return when(ev.action){
            MotionEvent.ACTION_DOWN -> {
                startYpos = ev.y
                super.dispatchTouchEvent(ev)
            }
            MotionEvent.ACTION_MOVE -> {
                val scrollYpos = (ev.y - startYpos).toInt()
                val pullDown = scrollYpos > 0 && canPullDown()
                val pullUp = scrollYpos < 0 && canPullUp()
                if (pullDown || pullUp) {
                    cancelChild(ev)
                    val offset = (scrollYpos * DAMPING_COEFFICIENT).toInt()
                    childView?.layout(original.left, original.top + offset, original.right, original.bottom + offset)
                    /*if (mScrollListener != null) {
                        mScrollListener.onScroll();
                    }*/
                    onScrollListener?.invoke(original.top+offset)
                    isMoved = true
                    isSuccess = false
                    return true
                } else {
                    startYpos = ev.y
                    isMoved = false
                    isSuccess = true
                    super.dispatchTouchEvent(ev)
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isMoved) {
                    recoverLayout()
                }
                !isSuccess || super.dispatchTouchEvent(ev)
            }
            else -> true
        }
    }

    /**
     * 取消子view已经处理的事件
     *
     * @param ev event
     */
    private fun cancelChild(ev:MotionEvent) {
        ev.action = MotionEvent.ACTION_CANCEL
        super.dispatchTouchEvent(ev)
    }

    /**
     * 位置还原
     */
    private fun recoverLayout() {
        childView?:return
        val anim = TranslateAnimation(0f, 0f, (childView!!.top - original.top).toFloat(), 0f)
        anim.duration = ANIM_TIME
        childView!!.startAnimation(anim)
        childView!!.layout(original.left, original.top, original.right, original.bottom)
        isMoved = false
    }

    /**
     * 判断是否可以下拉
     *
     * @return true：可以，false:不可以
     */
    private fun canPullDown():Boolean {
        childView?:return false
        with(childView!!){
            val firstVisiblePosition = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            if (firstVisiblePosition != 0 && adapter!!.itemCount != 0) {
                return false
            }
            val mostTop = if(childCount > 0) getChildAt(0).top else 0
            return mostTop >= 0
        }
    }

    /**
     * 判断是否可以上拉
     *
     * @return true：可以，false:不可以
     */
    private fun canPullUp():Boolean {
        childView?:return false
        with(childView!!) {
            val lastItemPosition = adapter!!.itemCount - 1
            val lastVisiblePosition =
                (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            if (lastVisiblePosition >= lastItemPosition) {
                val childIndex =
                    lastVisiblePosition - (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                val childCount = childCount
                val index = childIndex.coerceAtMost(childCount - 1)
                val lastVisibleChild = getChildAt(index)
                if (lastVisibleChild != null) {
                    return lastVisibleChild.bottom <= bottom - top
                }
            }
        }
        return false
    }

}