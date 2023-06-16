package cen.xiaoyuan.epub.core

data class EpubChapter(
    val url: String,
    val title: String,
    val body: String,
    val index:Int,
    val isSubtitle:Boolean = false,
    val subtitleId:String = ""
)