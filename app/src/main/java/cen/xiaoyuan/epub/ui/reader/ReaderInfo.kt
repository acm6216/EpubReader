package cen.xiaoyuan.epub.ui.reader

data class ReaderInfo(
    val textColorDisable:Boolean,
    val textColor:Int,
    val textSize:Int,
    val textActionMode:((String,Int,Int)->Unit)?,
    val imageActionMode:((ReaderItem.Image)->Unit)?
)