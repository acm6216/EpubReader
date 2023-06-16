package cen.xiaoyuan.epub.picker.adapters

import android.content.Context
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import cen.xiaoyuan.epub.picker.ColorAdapter

class BitmapColorAdapter(context: Context, @DrawableRes resource:Int): ColorAdapter {

    private val bitmap = BitmapFactory.decodeResource(context.resources, resource)
    override fun color(position: Int, shade: Int): Int {
        var tmp = shade
        var pixel = bitmap.getPixel(tmp, position)
        while (pixel == 0x0)
            pixel = bitmap.getPixel(tmp--, position)
        return pixel
    }

    override fun shades(position: Int): Int {
        val pixels = IntArray(bitmap.width)
        bitmap.getPixels(pixels, 0, bitmap.width, 0, position, bitmap.width, 1)
        for (i in pixels.indices)
            if (pixels[i] == 0x0) return i
        return bitmap.width
    }

    override fun size(): Int = bitmap.height

}