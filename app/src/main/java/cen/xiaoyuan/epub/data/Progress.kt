package cen.xiaoyuan.epub.data

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "epub_progress")
data class Progress(
    @PrimaryKey(autoGenerate = false)
    val id:Uri,
    val position:Long = 0,
    val chapterIndex:Int = 0
)