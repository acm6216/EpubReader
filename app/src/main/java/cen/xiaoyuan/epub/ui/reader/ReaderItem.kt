package cen.xiaoyuan.epub.ui.reader

import cen.xiaoyuan.epub.core.BookTextMapper

sealed interface ReaderItem {
    val chapterUrl: String
    val chapterIndex: Int

    sealed interface Position : ReaderItem {
        val chapterItemIndex: Int
    }

    enum class Location { FIRST, MIDDLE, LAST }
    sealed interface ParagraphLocation : ReaderItem {
        val location: Location
    }

    sealed interface Text : ReaderItem, Position {
        val text: String
    }

    data class End(
        override val chapterUrl: String,
        override val chapterIndex: Int
    ):ReaderItem

    data class Title(
        override val chapterUrl: String,
        override val chapterIndex: Int,
        override val chapterItemIndex: Int,
        override val text: String
    ) : ReaderItem, Text, Position

    data class Body(
        override val chapterUrl: String,
        override val chapterIndex: Int,
        override val chapterItemIndex: Int,
        override val text: String,
        override val location: Location,
        val navPointId:String = ""
    ) : ReaderItem, Text, Position, ParagraphLocation

    data class Image(
        override val chapterUrl: String,
        override val chapterIndex: Int,
        override val chapterItemIndex: Int,
        override val location: Location,
        val text: String,
        val image: BookTextMapper.ImgEntry
    ) : ReaderItem, Position, ParagraphLocation

    data class Padding(
        override val chapterIndex: Int,
        override val chapterUrl: String
    ):ReaderItem

    class Divider(override val chapterUrl: String, override val chapterIndex: Int) : ReaderItem
}

