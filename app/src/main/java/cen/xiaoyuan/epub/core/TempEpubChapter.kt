package cen.xiaoyuan.epub.core

data class TempEpubChapter(
    val url: String,
    val title: String?,
    val body: String,
    val chapterIndex: Int
)