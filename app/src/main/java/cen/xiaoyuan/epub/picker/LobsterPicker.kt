package cen.xiaoyuan.epub.picker

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.picker.adapters.BitmapColorAdapter
import kotlin.math.*

@SuppressLint("CustomViewStyleable")
class LobsterPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr:Int = 0
): View(context, attrs, defStyleAttr) {

    companion object {
        val EMPTY_CHAIN: Chain = object : Chain {
            override fun setColor(caller: ColorDecorator?, @ColorInt color: Int) {}
            override fun setShade(position: Int) {}
            override fun getAdapter(): ColorAdapter? {
                return null
            }

            override fun getAdapterPosition(): Int {
                return 0
            }

            override fun getShadePosition(): Int {
                return 0
            }
        }
    }

    private val chain: Chain = object : Chain {
        override fun setColor(caller: ColorDecorator?, @ColorInt color: Int) {
            val index: Int = decorators.indexOf(caller)
            if (index < decorators.size - 1) {
                decorators[index + 1].onColorChanged(this, color)
            } else {
                if (chainedColor != color) {
                    for (listener in listeners) {
                        listener.onColorChanged(color)
                    }
                }
                chainedColor = color
                invalidate()
            }
        }

        override fun setShade(position: Int) {
            shadePosition = position
            adapter?.color(colorPosition, shadePosition)?.also {
                color = it
                pointerPaint.color = it
            }
        }

        override fun getAdapter(): ColorAdapter? = adapter
        override fun getAdapterPosition(): Int = colorPosition
        override fun getShadePosition(): Int = shadePosition
    }

    private val updateDecorator: ColorDecorator = object : ColorDecorator {
        override fun onColorChanged(chain: Chain?, @ColorInt color: Int) {
            chain!!.setColor(this, color)
        }
    }

    private val decorators = ArrayList<ColorDecorator>()
    private val listeners = ArrayList<OnColorListener>()
    private var adapter: ColorAdapter? = null

    private var radius = 0
    private var pointerRadius = 0
    private var historyRadius = 0

    private var slopX = 0f
    private var slopY = 0f
    private var translationOffset = 0f

    private var pointerPressed = false
    private var wheelPressed = false

    private val pointerPosition = PointF()
    private val wheelRectangle = RectF()
    private val historyRectangle = RectF()

    private var colorPosition = 0
    private var shadePosition = 0

    private val wheelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private val historyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private var pointerShadow: Bitmap? = null

    private val largeRadiusPath = Path()
    private val smallRadiusPath = Path()
    private val tep = Path()

    private var color = 0
    private var chainedColor = 0
    private var historicColor = 0

    private var colorHistoryEnabled = false

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LobsterPicker, defStyleAttr, 0)
        val b = context.resources

        val thickness = a.getDimensionPixelSize(
            R.styleable.LobsterPicker_color_wheel_thickness,
            b.getDimensionPixelSize(R.dimen.color_wheel_thickness)
        )
        radius = a.getDimensionPixelSize(
            R.styleable.LobsterPicker_color_wheel_radius,
            b.getDimensionPixelSize(R.dimen.color_wheel_radius)
        )
        pointerRadius = a.getDimensionPixelSize(
            R.styleable.LobsterPicker_color_wheel_pointer_radius,
            b.getDimensionPixelSize(R.dimen.color_wheel_pointer_radius)
        )
        historyRadius = a.getDimensionPixelSize(
            R.styleable.LobsterPicker_color_history_radius,
            b.getDimensionPixelSize(R.dimen.color_history_radius)
        )
        colorHistoryEnabled = a.getBoolean(
            R.styleable.LobsterPicker_color_history_enabled,
            false
        )
        val pointerShadowRadius = a.getDimensionPixelSize(
            R.styleable.LobsterPicker_color_wheel_pointer_shadow_radius,
            b.getDimensionPixelSize(R.dimen.color_wheel_pointer_shadow_radius)
        )
        val pointerShadowColor = a.getColor(
            R.styleable.LobsterPicker_color_wheel_pointer_shadow,
            getColor(R.color.lobsterpicker_pointer_shadow, b)
        )
        val schemeRes = a.getResourceId(
            R.styleable.LobsterPicker_color_wheel_scheme,
            R.drawable.default_pallete
        )
        a.recycle()
        decorators.add(updateDecorator)
        wheelPaint.strokeWidth = thickness.toFloat()

        val pointerShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = pointerShadowColor
        }

        pointerShadow = Bitmap.createBitmap(
            pointerShadowRadius * 2,
            pointerShadowRadius * 2,
            Bitmap.Config.ARGB_8888
        ).also {
            Canvas(it).drawCircle(
                pointerShadowRadius.toFloat(),
                pointerShadowRadius.toFloat(),
                pointerShadowRadius.toFloat(),
                pointerShadowPaint
            )
        }
        largeRadiusPath.addCircle(0f, 0f, (radius + thickness / 2).toFloat(), Path.Direction.CW)
        largeRadiusPath.close()
        smallRadiusPath.addCircle(0f, 0f, (radius - thickness / 2).toFloat(), Path.Direction.CW)
        smallRadiusPath.close()

        adapter = BitmapColorAdapter(context, schemeRes)
        updateColorAdapter()
        invalidate()
    }

    private fun getColor(resId: Int, res: Resources): Int = ResourcesCompat.getColor(res, resId, res.newTheme())

    fun setColorAdapter(adapter: ColorAdapter) {
        val oldAngle: Float = getAngle(colorPosition)
        this.adapter = adapter
        updateColorAdapter()
        getMoveAnimation(oldAngle, getAngle(colorPosition)).start()
    }

    fun getColorAdapter(): ColorAdapter? = adapter

    fun addDecorator(decorator: ColorDecorator) {
        if (!decorators.contains(decorator)) {
            decorators.add(decorator)
            chainDecorators()
        }
    }

    fun removeDecorator(decorator: ColorDecorator): Boolean = decorators.remove(decorator)

    fun addOnColorListener(listener: OnColorListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeOnColorListener(listener: OnColorListener): Boolean = listeners.remove(listener)

    fun setColorHistoryEnabled(enabled: Boolean) {
        colorHistoryEnabled = enabled
        invalidate()
    }

    fun isColorFeedbackEnabled(): Boolean = colorHistoryEnabled

    fun setColor(@ColorInt color: Int) {
        val oldAngle: Float = getAngle(colorPosition)
        setClosestColorPosition(color)
        setPointerPosition(getAngle(colorPosition))
        chainedColor = adapter!!.color(colorPosition, shadePosition)
        this.color = chainedColor
        pointerPaint.color = this.color
        historyPaint.color = this.color
        chainDecorators(Color.alpha(color))
        getMoveAnimation(oldAngle, getAngle(colorPosition)).start()
    }

    @ColorInt
    fun getColor(): Int = chainedColor

    fun setHistory(@ColorInt color: Int) {
        historicColor = color
        invalidate()
    }

    @ColorInt
    fun getHistory(): Int = historicColor

    fun setColorPosition(position: Int) {
        val oldAngle: Float = getAngle(colorPosition)
        colorPosition = position
        setPointerPosition(getAngle(colorPosition))
        chainedColor = adapter!!.color(colorPosition, shadePosition)
        color = chainedColor
        pointerPaint.color = color
        historyPaint.color = color
        chainDecorators()
        getMoveAnimation(oldAngle, getAngle(colorPosition)).start()
    }

    fun getColorPosition(): Int = colorPosition

    fun setShadePosition(position: Int) {
        shadePosition = position
        chainedColor = adapter!!.color(colorPosition, shadePosition)
        color = chainedColor
        pointerPaint.color = color
        historyPaint.color = color
        chainDecorators()
    }

    fun getShadePosition(): Int = shadePosition

    private fun setClosestColorPosition(@ColorInt color: Int) {
        var closestDistance = Double.MAX_VALUE
        for (i in 0 until adapter!!.size()) {
            for (j in 0 until adapter!!.shades(i)) {
                val adapterColor = adapter!!.color(i, j)
                val distance = sqrt(
                    (Color.alpha(color) - Color.alpha(adapterColor)).toDouble().pow(2.0)
                            + (Color.red(color) - Color.red(adapterColor)).toDouble().pow(2.0)
                            + (Color.green(color) - Color.green(adapterColor)).toDouble().pow(2.0)
                            + (Color.blue(color) - Color.blue(adapterColor)).toDouble().pow(2.0)
                )
                if (distance < closestDistance) {
                    closestDistance = distance
                    colorPosition = i
                    shadePosition = j
                }
            }
        }
    }

    private fun updateColorAdapter() {
        if (colorPosition >= adapter!!.size()) {
            colorPosition = adapter!!.size() - 1
        }
        if (shadePosition >= adapter!!.shades(colorPosition)) {
            shadePosition = adapter!!.shades(colorPosition) - 1
        }
        setPointerPosition(getAngle(colorPosition))
        chainedColor = adapter!!.color(colorPosition, shadePosition)
        historicColor = chainedColor
        color = historicColor
        pointerPaint.color = color
        chainDecorators()
    }

    private fun chainDecorators() {
        for (decorator in decorators) {
            decorator.onColorChanged(chain, color)
        }
    }

    private fun chainDecorators(alpha: Int) {
        for (decorator in decorators) {
            color = color and 0x00FFFFFF
            color = color or (alpha shl 24)
            decorator.onColorChanged(chain, color)
        }
    }

    private fun setPointerPosition(radians: Float) {
        val x = (radius * cos(radians.toDouble())).toFloat()
        val y = (radius * sin(radians.toDouble())).toFloat()
        pointerPosition[x] = y
    }

    private fun getColorPosition(radians: Float): Int {
        var degrees = Math.toDegrees(radians.toDouble()).toInt() + 90
        degrees = if (degrees > 0) degrees else degrees + 360
        degrees = if (degrees == 360) 359 else degrees
        return (adapter!!.size() / 360.0f * degrees).toInt()
    }

    private fun getAngle(position: Int): Float {
        val nbOfSegments = adapter!!.size()
        val segmentWidth = 360 / nbOfSegments
        var degrees = position * segmentWidth + segmentWidth / 2 - 90
        if (degrees > 180) {
            degrees -= 360
        }
        return Math.toRadians(degrees.toDouble()).toFloat()
    }

    private fun getMoveAnimation(oldAngle: Float, newAngle: Float): ValueAnimator {
        val animator = ValueAnimator.ofFloat(oldAngle, newAngle)
        animator.addUpdateListener { animation: ValueAnimator ->
            setPointerPosition(animation.animatedValue as Float)
            invalidate()
        }
        return animator
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val intrinsicSize = 2 * (radius + pointerRadius)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val width: Int = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> intrinsicSize.coerceAtMost(widthSize)
            else -> intrinsicSize
        }
        val height: Int = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> intrinsicSize.coerceAtMost(heightSize)
            else -> intrinsicSize
        }
        val min = width.coerceAtMost(height)
        setMeasuredDimension(min, min)
        translationOffset = min * 0.5f
        wheelRectangle[-radius.toFloat(), -radius.toFloat(), radius.toFloat()] = radius
            .toFloat()
        historyRectangle[-historyRadius.toFloat(), -historyRadius.toFloat(), historyRadius.toFloat()] =
            historyRadius
                .toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.translate(translationOffset, translationOffset)
        val nbOfSegments = adapter!!.size()
        val segmentWidth = 360 / nbOfSegments
        for (i in 0 until nbOfSegments) {
            wheelPaint.color = adapter!!.color(i, shadePosition)
            canvas.drawArc(
                wheelRectangle, (
                        segmentWidth * i - 90 - if (i == 0) 1 else 0).toFloat(), (
                        segmentWidth + if (i < nbOfSegments - 1) 1 else 0).toFloat(),
                false, wheelPaint
            )
        }
        if (colorHistoryEnabled) {
            historyPaint.color = historicColor
            canvas.drawArc(historyRectangle, -90f, 180f, true, historyPaint)
            historyPaint.color = chainedColor
            canvas.drawArc(historyRectangle, 90f, 180f, true, historyPaint)
        }
        canvas.save()
        tep.op(largeRadiusPath, Path.Op.UNION)
        tep.op(smallRadiusPath, Path.Op.XOR)
        canvas.clipPath(tep)
        canvas.drawBitmap(
            pointerShadow!!,
            pointerPosition.x - pointerShadow!!.width / 2f,
            pointerPosition.y - pointerShadow!!.height / 2f, null
        )
        canvas.restore()
        canvas.drawCircle(
            pointerPosition.x, pointerPosition.y,
            pointerRadius.toFloat(), pointerPaint
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        val x = event.x - translationOffset
        val y = event.y - translationOffset
        when (event.action) {
            MotionEvent.ACTION_DOWN ->
                if (x >= pointerPosition.x - pointerRadius && x <= pointerPosition.x + pointerRadius && y >= pointerPosition.y - pointerRadius && y <= pointerPosition.y + pointerRadius) {
                    slopX = x - pointerPosition.x
                    slopY = y - pointerPosition.y
                    pointerPressed = true
                } else if (sqrt((x * x + y * y).toDouble()) <= radius + pointerRadius
                    && sqrt((x * x + y * y).toDouble()) >= radius - pointerRadius
                ) {
                    wheelPressed = true
                    val angle = atan2((y - slopY).toDouble(), (x - slopX).toDouble()).toFloat()
                    setPointerPosition(angle)
                    colorPosition = getColorPosition(angle)
                    adapter?.color(colorPosition, shadePosition)?.apply {
                        color = this
                        pointerPaint.color = color
                        historyPaint.color = color
                    }
                    chainDecorators()
                    invalidate()
                } else {
                    parent.requestDisallowInterceptTouchEvent(false)
                    return false
                }
            MotionEvent.ACTION_MOVE -> if (pointerPressed || wheelPressed) {
                val angle = atan2((y - slopY).toDouble(), (x - slopX).toDouble()).toFloat()
                setPointerPosition(angle)
                colorPosition = getColorPosition(angle)
                adapter?.color(colorPosition, shadePosition)?.apply {
                    color = this
                    pointerPaint.color = color
                    historyPaint.color = color
                }
                chainDecorators()
                invalidate()
            } else {
                parent.requestDisallowInterceptTouchEvent(false)
                return false
            }
            MotionEvent.ACTION_UP -> {
                val angle = atan2((y - slopY).toDouble(), (x - slopX).toDouble()).toFloat()
                if (wheelPressed) {
                    setPointerPosition(angle)
                    colorPosition = getColorPosition(angle)
                    color = adapter!!.color(colorPosition, shadePosition)
                    pointerPaint.color = color
                    historyPaint.color = color
                    chainDecorators()
                }
                wheelPressed = false
                pointerPressed = false
                listeners.forEach { it.onColorSelected(chainedColor) }
                getMoveAnimation(angle, getAngle(colorPosition)).start()
            }
        }
        return true
    }

}