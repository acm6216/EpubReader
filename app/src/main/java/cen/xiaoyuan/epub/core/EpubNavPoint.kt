package cen.xiaoyuan.epub.core

data class EpubNavPoint(
    val id:String,
    val playOrder:String,
    val url:String,
    val body:String,
    val title:String?,
    val chapterIndex:Int,
    val isSubNavPoint:Boolean = false,
    val subNavPointId:String = ""
)