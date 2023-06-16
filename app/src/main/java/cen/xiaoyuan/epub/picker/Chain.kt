package cen.xiaoyuan.epub.picker

import androidx.annotation.ColorInt
import cen.xiaoyuan.epub.picker.ColorAdapter
import cen.xiaoyuan.epub.picker.ColorDecorator

interface Chain {
    fun setColor(caller: ColorDecorator?, @ColorInt color: Int)

    fun setShade(position: Int)

    fun getAdapter(): ColorAdapter?

    fun getAdapterPosition(): Int

    fun getShadePosition(): Int
}