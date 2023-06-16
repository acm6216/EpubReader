package cen.xiaoyuan.epub.ui.reader.nav

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.data.Note
import cen.xiaoyuan.epub.databinding.EpubSimpleListBinding
import cen.xiaoyuan.epub.ui.BaseFragment
import cen.xiaoyuan.epub.ui.adapters.BookNoteAdapter
import cen.xiaoyuan.epub.ui.reader.ReaderViewModel
import kotlinx.coroutines.launch

class EpubBookNote: BaseFragment<EpubSimpleListBinding>() {

    private val reader: ReaderViewModel by activityViewModels()

    private val noteMore:((Note,Int)->Unit) = { note, id ->
        when (id) {
            R.id.menu_book_note_delete -> reader.deleteNotes(note)
            R.id.menu_book_note_export -> reader.exportNotes(note)
            else -> Unit
        }
    }
    private val noteClick:((Note)->Unit) = { note ->
        //reader.openItemByBookmark(bookmark)
    }

    private val bookNoteAdapter = BookNoteAdapter(click = noteClick, more =  noteMore)

    override fun setBinding() = EpubSimpleListBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = bookNoteAdapter

        repeatWithViewLifecycle {
            launch {
                reader.currentBookNotes.collect{
                    bookNoteAdapter.submitList(it)
                }
            }
        }
    }
}
