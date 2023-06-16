package cen.xiaoyuan.epub.picker

import androidx.annotation.ColorInt

interface ColorAdapter {
    @ColorInt
    fun color(position: Int, shade: Int): Int
    fun shades(position: Int): Int
    fun size(): Int
}