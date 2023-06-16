package cen.xiaoyuan.epub.data

import android.net.Uri
import androidx.room.DatabaseView
import androidx.room.Relation
import cen.xiaoyuan.epub.core.EpubInfo

@DatabaseView(
    """
        SELECT b.id, b.title, b.info
        FROM epub_books AS b
    """
)
data class NoteDetail(
    val id: Uri,
    val title:String,
    val info: EpubInfo,

    @Relation(
        parentColumn = "id",
        entity = Note::class,
        entityColumn = "bookUri"
    )
    val notes:List<Note>
)