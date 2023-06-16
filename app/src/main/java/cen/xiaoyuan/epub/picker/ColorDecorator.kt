package cen.xiaoyuan.epub.picker

import androidx.annotation.ColorInt

interface ColorDecorator {
    fun onColorChanged(chain: Chain?, @ColorInt color: Int)
}