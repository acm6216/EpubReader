package cen.xiaoyuan.epub.ui.reader

import cen.xiaoyuan.epub.core.EpubChapter

data class EpubChapterReader(val chapters: List<EpubChapter>,val currentChapter: EpubChapter,val index:Int)