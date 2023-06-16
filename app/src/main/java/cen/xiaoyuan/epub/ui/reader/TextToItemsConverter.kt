package cen.xiaoyuan.epub.ui.reader

import cen.xiaoyuan.epub.core.BookTextMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

suspend fun textToItemsConverter(
    chapterUrl: String,
    chapterPos: Int,
    initialChapterItemIndex: Int,
    text: String
): List<ReaderItem> = withContext(Dispatchers.Default) {
    val paragraphs = text
        .splitToSequence("\n\n")
        .filter { it.isNotBlank() }
        .toList()

    return@withContext paragraphs
        .mapIndexed { index, paragraph ->
            async {
                generateITEM(
                    chapterUrl = chapterUrl,
                    chapterIndex = chapterPos,
                    chapterItemIndex = index + initialChapterItemIndex,
                    text = paragraph.navPointText(),
                    location = when (index) {
                        0 -> ReaderItem.Location.FIRST
                        paragraphs.lastIndex -> ReaderItem.Location.LAST
                        else -> ReaderItem.Location.MIDDLE
                    },
                    navPointId = paragraph.navPointId()
                )
            }
        }.awaitAll()
}

private fun String.navPointText():String = if(this.contains("#id=")) this.substring(1,this.indexOf("#id=")) else this

private fun String.navPointId() = if(this.contains("#id=")) this.substring(this.indexOf("#id=")+4,this.length-1) else ""

private fun generateITEM(
    chapterUrl: String,
    chapterIndex: Int,
    chapterItemIndex: Int,
    text: String,
    location: ReaderItem.Location,
    navPointId:String
): ReaderItem = when (val imgEntry = BookTextMapper.ImgEntry.fromXMLString(text)) {
    null -> ReaderItem.Body(
        chapterUrl = chapterUrl,
        chapterIndex = chapterIndex,
        chapterItemIndex = chapterItemIndex,
        text = "${if(navPointId.isNotEmpty()) "" else "\u3000\u3000"}$text\n",
        location = location,
        navPointId = navPointId
    )
    else -> ReaderItem.Image(
        chapterUrl = chapterUrl,
        chapterIndex = chapterIndex,
        chapterItemIndex = chapterItemIndex,
        text = text,
        location = location,
        image = imgEntry
    )
}
