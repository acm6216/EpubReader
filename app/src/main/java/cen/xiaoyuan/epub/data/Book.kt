package cen.xiaoyuan.epub.data

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import cen.xiaoyuan.epub.core.EpubInfo

@Entity(tableName = "epub_books")
data class Book(
    @PrimaryKey(autoGenerate = false)
    val id: Uri,
    val title:String,
    val coverPath:String,
    val fileName:String,
    val chapters:String,
    val info:EpubInfo,
    val lastReadingTime:Long,
    val readStatus:Boolean,
    val isFavorite:Boolean
)