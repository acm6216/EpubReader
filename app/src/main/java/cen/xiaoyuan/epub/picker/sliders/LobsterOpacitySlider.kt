package cen.xiaoyuan.epub.picker.sliders

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import cen.xiaoyuan.epub.picker.Chain
import cen.xiaoyuan.epub.picker.LobsterPicker
import cen.xiaoyuan.epub.picker.LobsterSlider

class LobsterOpacitySlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr:Int = 0
): LobsterSlider(context, attrs, defStyleAttr) {

    private var chain = LobsterPicker.EMPTY_CHAIN
    private val pointerPath = Path()
    private var opacity = 255
    private val hsv = FloatArray(3)

    private var chainedColor = 0xFF000000.toInt()
    private var pointerPressed = false

    init {
        updateShader()
        invalidate()
    }

    override fun onColorChanged(chain: Chain?, color: Int) {
        chain?.also { this.chain = it }
        this.chainedColor = color
        pointerPaint.color = color
        opacity = ((pointerPosition.x.toFloat() / length) * 255).toInt()
        updateShader()
        updateColor()
        chain?.setColor(this,chainedColor)
        if (Color.alpha(color) != 0xFF) {
            setOpacity(Color.alpha(color))
        }
        invalidate()
    }

    override fun getColor(): Int = chainedColor

    private fun setOpacity(opacity: Int) {
        this.opacity = opacity
        updateColor()
        chain.setColor(this, chainedColor)
        getMoveAnimation()?.start()
    }

    private fun updateShader() {
        paint.shader = LinearGradient(
            0f, 0f,
            length.toFloat(), 0f, intArrayOf(0x00FFFFFF and chainedColor, chainedColor), null,
            Shader.TileMode.CLAMP
        )
    }

    private fun updateColor() {
        Color.colorToHSV(chainedColor, hsv)
        chainedColor = Color.HSVToColor(opacity, hsv)
        pointerPaint.color = chainedColor
        pointerShadowPaint.color = Color.HSVToColor(0x59, hsv)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.translate(pointerShadowRadius.toFloat(), (height / 2).toFloat())
        canvas.save()
        pointerPath.reset()
        pointerPath.addCircle(
            pointerPosition.x.toFloat(),
            pointerPosition.y.toFloat(),
            pointerRadius.toFloat(),
            Path.Direction.CW
        )
        pointerPath.close()
        canvas.clipPath(pointerPath)
        canvas.drawLine(0f, 0f, length.toFloat(), 0f, paint)
        canvas.drawCircle(
            pointerPosition.x.toFloat(),
            pointerPosition.y.toFloat(),
            pointerShadowRadius.toFloat(),
            pointerShadowPaint
        )
        canvas.restore()
        canvas.drawCircle(
            pointerPosition.x.toFloat(),
            pointerPosition.y.toFloat(),
            pointerRadius.toFloat(),
            pointerPaint
        )
    }

    private fun getMoveAnimation(): ValueAnimator? {
        val animator = ValueAnimator.ofInt(
            pointerPosition.x,
            (opacity.toFloat() / 255f * length.toFloat()).toInt()
        )
        animator.addUpdateListener { animation ->
            pointerPosition.x = (animation.animatedValue as Int)
            invalidate()
        }
        return animator
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (x >= pointerShadowRadius && x <= pointerShadowRadius + length) {
                    pointerPosition.x = x.toInt() - pointerShadowRadius
                } else if (x < pointerShadowRadius) {
                    pointerPosition.x = 0
                } else if (x > pointerShadowRadius + length) {
                    pointerPosition.x = length
                }
                pointerPressed = true
                opacity = (pointerPosition.x.toFloat() / length * 255).toInt()
                updateColor()
                chain.setColor(this, chainedColor)
                getGrowAnimation().start()
            }
            MotionEvent.ACTION_MOVE -> if (pointerPressed) {
                if (x >= pointerShadowRadius && x <= pointerShadowRadius + length) {
                    pointerPosition.x = x.toInt() - pointerShadowRadius
                } else if (x < pointerShadowRadius) {
                    pointerPosition.x = 0
                } else if (x > pointerShadowRadius + length) {
                    pointerPosition.x = length
                }
                opacity = (pointerPosition.x.toFloat() / length * 255).toInt()
                updateColor()
                chain.setColor(this, chainedColor)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                if (pointerPressed) {
                    pointerPosition.x = x.toInt() - pointerShadowRadius
                    if (pointerPosition.x > length) {
                        pointerPosition.x = length
                    } else if (pointerPosition.x < 0) {
                        pointerPosition.x = 0
                    }
                    opacity = (pointerPosition.x.toFloat() / length * 255).toInt()
                    updateColor()
                    chain.setColor(this, chainedColor)
                    getShrinkAnimation().start()
                }
                pointerPressed = false
            }
        }
        return true
    }
}