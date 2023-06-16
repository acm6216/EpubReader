package cen.xiaoyuan.epub.utils

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.data.Book
import cen.xiaoyuan.epub.ui.reader.ImageItem
import coil.load
import coil.loadAny
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("fadeToVisibilityUnsafe")
fun View.indicatorFadeToVisibilityUnsafe(fade:Boolean){
    fadeToVisibilityUnsafe(fade)
}

@BindingAdapter("isGone")
fun View.setIsGone(
    isGone: Boolean
) {
    visibility = if (isGone) View.GONE else View.VISIBLE
}

@BindingAdapter(value = ["loadCover", "samplingValue"], requireAll = false)
fun ImageView.loadCover(book: Book?, samplingValue: Int = -1) {
    book ?: return
    load(File(book.coverPath)){
        size(if(samplingValue>0) samplingValue else 240)
        crossfade(true)
        crossfade(250)
    }
}

@BindingAdapter("load_image")
fun ImageView.loadImage(imageItem: ImageItem?){
    imageItem?:return
    loadAny(imageItem){
        allowHardware(false)
        target(onSuccess = {
            setImageDrawable(it)
        }, onError = {
            setImageResource(R.drawable.ic_menu_book)
        }).build()
        crossfade(true)
        crossfade(250)
    }
}

@BindingAdapter("date_text")
fun TextView.setDateText(long: Long){
    text = long.stampToDate()
}
fun Long.stampToDate():String{
    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    val date = Date(this)
    return simpleDateFormat.format(date)
}