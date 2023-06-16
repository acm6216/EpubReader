package cen.xiaoyuan.epub.ui.excerpt

import android.net.Uri

sealed interface ExcerptItem{
    val uri:Uri
    val title:String
    val creator:String
    val type:Int

    data class ExcerptHeader(
        override val uri: Uri,
        override val title: String,
        override val creator: String,
        override val type: Int = 0,
        var expanded: Boolean,
        val count:Int
    ): ExcerptItem

    data class ExcerptContent(
        override val uri: Uri,
        override val title: String,
        override val creator: String,
        override val type: Int = 1,
        val content:String,
        val date:Long
    ):ExcerptItem
}