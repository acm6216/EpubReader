package cen.xiaoyuan.epub.core

data class EpubBook(
    val fileName: String,
    val title: String,
    val coverImagePath: String,
    val chapters: List<EpubChapter>,
    val chapterNavPoints: List<EpubNavPoint>,
    val images: List<EpubImage>,
    val info:EpubInfo
)