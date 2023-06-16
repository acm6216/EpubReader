package cen.xiaoyuan.epub.picker.sliders

import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.ColorInt
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.picker.Chain
import cen.xiaoyuan.epub.picker.ColorAdapter
import cen.xiaoyuan.epub.picker.ColorDecorator
import cen.xiaoyuan.epub.picker.LobsterPicker
import cen.xiaoyuan.epub.picker.LobsterSlider
import cen.xiaoyuan.epub.picker.OnColorListener
import cen.xiaoyuan.epub.picker.adapters.BitmapColorAdapter
import kotlin.math.pow
import kotlin.math.sqrt

class LobsterShadeSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr:Int = 0
): LobsterSlider(context, attrs, defStyleAttr) {

    private val chain = object : Chain {
        override fun setColor(caller: ColorDecorator?, color: Int) {
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
            }
        }

        override fun setShade(position: Int) {}

        override fun getAdapter(): ColorAdapter? = adapter

        override fun getAdapterPosition(): Int = 0

        override fun getShadePosition(): Int = shadePosition
    }

    private val updateDecorator = object : ColorDecorator {
        override fun onColorChanged(chain: Chain?, color: Int) {
            chain?.setColor(this, color)
        }
    }

    private val decorators = ArrayList<ColorDecorator>()
    private val listeners = ArrayList<OnColorListener>()

    private var lobsterPickerChain = LobsterPicker.EMPTY_CHAIN
    private var adapter: ColorAdapter? = null
    private var currentAnimation: ValueAnimator? = null
    private var pointerPressed = false

    private var shades: IntArray? = null
    private var segmentLength = 0
    private var shadePosition = 0
    private var currentAnimationEnd = 0
    private var chainedColor = 0

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.LobsterShadeSlider, defStyleAttr, 0)
        val schemeRes = a.getResourceId(R.styleable.LobsterShadeSlider_color_slider_scheme, R.drawable.default_shader_pallete)
        a.recycle()
        adapter = BitmapColorAdapter(context, schemeRes).also {
            shadePosition = it.shades(0) - 1
        }
        decorators?.add(updateDecorator)
        updateColorAdapter()
        pointerPosition.x = (segmentLength * shadePosition) + (segmentLength / 2)
        invalidate()
    }

    override fun onColorChanged(chain: Chain?, color: Int) {
        chain?.getAdapter()?.shades(chain.getAdapterPosition())?.also { size ->
            lobsterPickerChain = chain
            segmentLength = length / size
            shades = IntArray(size)
            for (i in 0 until size) {
                shades!![size - 1 - i] = chain.getAdapter()!!.color(chain.getAdapterPosition(), i)
            }
            shadePosition = size - 1 - chain.getShadePosition()
            if (shadePosition == shades!!.size) {
                shadePosition--
            } else if (shadePosition < 0) {
                shadePosition = 0
            }
            updatePointer()
            chain.setShade(getShadePosition())
            chain.setColor(this, shades!![shadePosition])
            if (chainedColor != shades!![shadePosition]) {
                listeners.forEach { it.onColorChanged(chainedColor) }
            }
            chainedColor = shades!![shadePosition]
            val upcomingPosition = segmentLength * shadePosition + segmentLength / 2
            if (upcomingPosition != currentAnimationEnd) {
                currentAnimation?.run{ if(isStarted) cancel() }
                currentAnimation = getMoveAnimation()
                currentAnimation!!.start()
            }
            invalidate()
        }
    }

    override fun getColor(): Int = chainedColor

    fun setColor(@ColorInt color: Int) {
        setClosestColorPosition(color)
        val oldColor = chainedColor
        updatePointer()
        chainDecorators(Color.alpha(color))
        if (chainedColor != oldColor) {
            for (listener in listeners) {
                listener.onColorSelected(chainedColor)
            }
        }
        getMoveAnimation()?.start()
    }

    fun setColorAdapter(adapter: ColorAdapter) {
        val oldColor = chainedColor
        this.adapter = adapter
        if (getShadePosition() >= adapter.shades(0) - 1) {
            shadePosition = 0
        } else if (shadePosition >= adapter.shades(0)) {
            shadePosition = adapter.shades(0) - 1 - getShadePosition()
        }
        updateColorAdapter()
        if (chainedColor != oldColor) {
            for (listener in listeners) {
                listener.onColorSelected(chainedColor)
            }
        }
        getMoveAnimation()!!.start()
    }

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

    fun setShadePosition(position: Int) {
        shadePosition = shades!!.size - 1 - position
        val oldColor = chainedColor
        updateShade()
        if (chainedColor != oldColor) {
            listeners.forEach { it.onColorChanged(chainedColor) }
        }
        getMoveAnimation()!!.start()
    }

    private fun getShadePosition():Int = shades!!.size - 1 - shadePosition

    private fun setClosestColorPosition(@ColorInt color: Int) {
        var closestDistance = Double.MAX_VALUE
        for (i in 0 until adapter!!.shades(0)) {
            val adapterColor = adapter!!.color(0, i)
            val distance = sqrt(
                (Color.alpha(color) - Color.alpha(adapterColor)).toDouble().pow(2.0)
                        + (Color.red(color) - Color.red(adapterColor)).toDouble().pow(2.0)
                        + (Color.green(color) - Color.green(adapterColor)).toDouble().pow(2.0)
                        + (Color.blue(color) - Color.blue(adapterColor)).toDouble().pow(2.0)
            )
            if (distance < closestDistance) {
                closestDistance = distance
                shadePosition = adapter!!.shades(0) - 1 - i
            }
        }
    }

    private fun updateColorAdapter() {
        if (lobsterPickerChain === LobsterPicker.EMPTY_CHAIN) {
            val size = adapter!!.shades(0)
            segmentLength = length / size
            shades = IntArray(size)
            for (i in 0 until size) {
                shades!![size - 1 - i] = adapter!!.color(0, i)
            }
            updatePointer()
            chainDecorators()
        }
    }

    private fun updateShade() {
        updatePointer()
        lobsterPickerChain.setShade(getShadePosition())
        lobsterPickerChain.setColor(this, shades!![shadePosition])
        chainDecorators()
    }

    private fun updatePointer() {
        val color = shades!![shadePosition]
        pointerPaint.color = shades!![shadePosition]
        pointerShadowPaint.color =
            Color.argb(0x59, Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun chainDecorators() {
        for (decorator in decorators) {
            decorator.onColorChanged(chain, shades!![shadePosition])
        }
    }

    private fun chainDecorators(alpha: Int) {
        for (decorator in decorators) {
            shades!![shadePosition] = shades!![shadePosition] and 0x00FFFFFF
            shades!![shadePosition] = shades!![shadePosition] or (alpha shl 24)
            decorator.onColorChanged(chain, shades!![shadePosition])
        }
    }

    private fun findShadePosition(): Int {
        var position = (shades!!.size.toFloat() / length * pointerPosition.x).toInt()
        if (position == shades!!.size) {
            position--
        }
        return position
    }

    override fun onDraw(canvas: Canvas) {
        canvas.translate(pointerShadowRadius.toFloat(), (height / 2).toFloat())
        for (i in shades!!.indices) {
            paint.color = shades!![i]
            canvas.drawLine(
                (segmentLength * i).toFloat(),
                0f,
                (segmentLength * (i + 1)).toFloat(),
                0f,
                paint
            )
        }
        canvas.drawCircle(
            pointerPosition.x.toFloat(),
            pointerPosition.y.toFloat(),
            pointerShadowRadius.toFloat(),
            pointerShadowPaint
        )
        canvas.drawCircle(
            pointerPosition.x.toFloat(),
            pointerPosition.y.toFloat(),
            pointerRadius.toFloat(),
            pointerPaint
        )
    }

    private fun getMoveAnimation(): ValueAnimator? {
        val animator = ValueAnimator.ofInt(pointerPosition.x,
            segmentLength * shadePosition + (segmentLength / 2).also {
                currentAnimationEnd = it
            })
        animator.addUpdateListener { animation ->
            pointerPosition.x = (animation.animatedValue as Int)
            invalidate()
        }
        return animator
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(true)
        val x = event.x
        val newShade: Int
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
                newShade = findShadePosition()
                if (newShade != shadePosition) {
                    shadePosition = newShade
                    updateShade()
                }
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
                newShade = findShadePosition()
                if (newShade != shadePosition) {
                    shadePosition = newShade
                    updateShade()
                }
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
                    newShade = findShadePosition()
                    if (newShade != shadePosition) {
                        shadePosition = newShade
                        updateShade()
                    }
                    val set = AnimatorSet()
                    set.playTogether(getMoveAnimation(), getShrinkAnimation())
                    set.start()
                }
                pointerPressed = false
                for (listener in listeners) {
                    listener.onColorSelected(chainedColor)
                }
            }
        }
        return true
    }
}