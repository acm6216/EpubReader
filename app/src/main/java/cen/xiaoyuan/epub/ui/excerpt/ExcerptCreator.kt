package cen.xiaoyuan.epub.ui.excerpt

import cen.xiaoyuan.epub.data.Note

class ExcerptCreator {

    fun execute(notes:List<Note>,expanded: List<Expand>):List<ExcerptItem>{
        val result = ArrayList<ExcerptItem>()
        //head
        expanded.forEach { expand ->
            //sub note
            val sub = notes.filter { it.bookUri == expand.uri }
            val note = notes.first { it.bookUri == expand.uri }
            ExcerptItem.ExcerptHeader(
                uri = expand.uri,
                title = note.bookTitle,
                creator = note.creator,
                count = sub.size,
                expanded = expand.expanded
            ).also { result.add(it) }
            if(expand.expanded)
                sub.map {
                    ExcerptItem.ExcerptContent(
                        uri = it.bookUri,
                        title = it.bookTitle,
                        creator = it.creator,
                        content = it.content,
                        date = it.date
                    )
                }.also { result.addAll(it) }
        }
        return result
    }
}