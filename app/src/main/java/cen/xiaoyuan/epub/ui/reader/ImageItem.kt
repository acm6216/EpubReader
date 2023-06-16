package cen.xiaoyuan.epub.ui.reader

import cen.xiaoyuan.epub.core.EpubBook

data class ImageItem(val epubBook: EpubBook,val image: ReaderItem.Image){
    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }
}