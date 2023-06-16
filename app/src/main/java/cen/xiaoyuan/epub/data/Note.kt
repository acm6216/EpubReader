package cen.xiaoyuan.epub.data

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "epub_notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id:Long,
    val bookUri: Uri,
    val bookTitle:String,
    val creator:String,
    val content:String,
    val date:Long
)