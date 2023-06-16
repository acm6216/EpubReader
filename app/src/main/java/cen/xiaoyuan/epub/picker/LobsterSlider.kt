package cen.xiaoyuan.epub.picker

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.graphics.Point
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import cen.xiaoyuan.epub.R

@SuppressLint("CustomViewStyleable")
abstract class LobsterSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), ColorDecorator {

    private var thickness = 0
    protected var length = 0

    protected var pointerRadius = 0
    private var originalPointerRadius = 0
    protected var pointerShadowRadius = 0

    protected var pointerPosition: Point

    protected val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    protected val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style =  Paint.Style.FILL }
    protected val pointerShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LobsterSlider, defStyleAttr, 0)
        val b = context.resources
        thickness = a.getDimensionPixelSize(
            R.styleable.LobsterSlider_color_slider_thickness,
            b.getDimensionPixelSize(R.dimen.color_slider_thickness)
        )
        length = a.getDimensionPixelSize(
            R.styleable.LobsterSlider_color_slider_length,
            b.getDimensionPixelSize(R.dimen.color_slider_length)
        )
        pointerRadius = a.getDimensionPixelSize(
            R.styleable.LobsterSlider_color_slider_pointer_radius,
            b.getDimensionPixelSize(R.dimen.color_slider_pointer_radius)
        ).also { originalPointerRadius = it }
        pointerShadowRadius = a.getDimensionPixelSize(
            R.styleable.LobsterSlider_color_slider_pointer_shadow_radius,
            b.getDimensionPixelSize(R.dimen.color_slider_pointer_shadow_radius)
        )
        a.recycle()

        paint.strokeWidth = thickness.toFloat()
        pointerPosition = Point(length, 0)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val intrinsicWidth = length + pointerShadowRadius * 2
        val intrinsicHeight = pointerShadowRadius * 2
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width: Int = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> intrinsicWidth.coerceAtMost(widthSize)
            else -> intrinsicWidth
        }
        val height: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> intrinsicHeight.coerceAtMost(heightSize)
            else -> intrinsicHeight
        }
        setMeasuredDimension(width, height)
    }

    protected open fun getShrinkAnimation(): ValueAnimator {
        val animator = ValueAnimator.ofInt(pointerRadius, originalPointerRadius)
        animator.addUpdateListener { animation: ValueAnimator ->
            pointerRadius = animation.animatedValue as Int
            invalidate()
        }
        return animator
    }

    protected open fun getGrowAnimation(): ValueAnimator {
        val animator = ValueAnimator.ofInt(pointerRadius, pointerShadowRadius)
        animator.addUpdateListener { animation: ValueAnimator ->
            pointerRadius = animation.animatedValue as Int
            invalidate()
        }
        return animator
    }

    @ColorInt
    abstract fun getColor(): Int
}