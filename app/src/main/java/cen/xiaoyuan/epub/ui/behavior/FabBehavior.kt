package cen.xiaoyuan.epub.ui.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat

class FabBehavior(context: Context,attrs: AttributeSet):CoordinatorLayout.Behavior<View>(context, attrs) {

    private var visible = true

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL || super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, axes, type)
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        super.onNestedScroll(
            coordinatorLayout, child, target,
            dxConsumed, dyConsumed, dxUnconsumed,
            dyUnconsumed, type, consumed
        )
        if (dyConsumed > 0 && visible) {
            visible = false
            animateOut(child)
        } else if (dyConsumed < 0 && !visible) {
            visible = true
            animateIn(child)
        }
    }

    private fun animateOut(view:View) {
        val layoutParams = view.layoutParams as CoordinatorLayout.LayoutParams
        view.animate().translationY((view.height + layoutParams.bottomMargin).toFloat()).interpolator =
            AccelerateInterpolator(3f)
        ViewCompat.animate(view).scaleX(0f).scaleY(0f).start()
    }

    private fun animateIn(view:View) {
        view.animate().translationY(0f).interpolator = DecelerateInterpolator(3f)
        ViewCompat.animate(view).scaleX(1f).scaleY(1f).start()
    }

}