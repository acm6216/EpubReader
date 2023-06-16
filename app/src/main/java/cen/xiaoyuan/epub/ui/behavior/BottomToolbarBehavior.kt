package cen.xiaoyuan.epub.ui.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.RecyclerView

class BottomToolbarBehavior: CoordinatorLayout.Behavior<View>{

    private var deltaY:Float = 0f

    constructor():super()
    constructor(context: Context, attributeSet: AttributeSet):super(context,attributeSet)

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        return dependency is RecyclerView
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: View,
        dependency: View
    ): Boolean {
        return super.onDependentViewChanged(parent, child, dependency)
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        onDependentViewChanged(coordinatorLayout,child,directTargetChild)
        return axes== ViewCompat.SCROLL_AXIS_VERTICAL ||super.onStartNestedScroll(
            coordinatorLayout, child,
            directTargetChild,
            target, axes, type
        )
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout, child: View,
        target: View, dxConsumed: Int, dyConsumed: Int,
        dxUnconsumed: Int, dyUnconsumed: Int,
        type: Int, consumed: IntArray
    ) {
        super.onNestedScroll(
            coordinatorLayout, child, target,
            dxConsumed, dyConsumed, dxUnconsumed,
            dyUnconsumed, type, consumed
        )

        deltaY+=dyConsumed
        deltaY = if(deltaY>child.height) child.height.toFloat()
        else if(deltaY<0f) 0f
        else deltaY

        child.translationY = deltaY
    }

}