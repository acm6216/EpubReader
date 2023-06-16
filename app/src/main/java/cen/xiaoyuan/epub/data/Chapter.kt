package cen.xiaoyuan.epub.data

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "epub_chapters")
data class Chapter(
    @PrimaryKey(autoGenerate = true)
    val id:Long,
    val bookUri: Uri,
    val chapterTitle:String,
    val itemIndex:Long,
    val chapterIndex:Long,
    val currentChapterItem:Long,
    val isSubtitle:Boolean,
    val subtitleId:String
)