package cen.xiaoyuan.epub.ui.reader

data class ChapterDetail(
    val chapterTitle:String,
    val chapterProgress:String
){
    companion object{
        fun EMPTY():ChapterDetail = ChapterDetail("NULL","0%")
    }
}