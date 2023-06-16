package cen.xiaoyuan.epub.data

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "epub_bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true)
    val id:Long,
    val bookUri: Uri,
    val text:String,
    val description:String,
    val date:Long,
    val itemIndex:Int
)