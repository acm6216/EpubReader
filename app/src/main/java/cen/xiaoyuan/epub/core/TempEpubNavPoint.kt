package cen.xiaoyuan.epub.core

data class TempEpubNavPoint(
    val id:String,
    val playOrder:String,
    val url:String,
    val title:String?,
    val chapterIndex:Int,
    val isSubNavPoint:Boolean = false,
    val subNavPointId:String = ""
)