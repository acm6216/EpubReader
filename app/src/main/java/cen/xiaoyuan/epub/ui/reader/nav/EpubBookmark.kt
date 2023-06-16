package cen.xiaoyuan.epub.ui.reader.nav

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.databinding.EpubSimpleListBinding
import cen.xiaoyuan.epub.ui.BaseFragment
import cen.xiaoyuan.epub.ui.adapters.BookmarkAdapter
import cen.xiaoyuan.epub.ui.reader.ReaderViewModel
import kotlinx.coroutines.launch

class EpubBookmark: BaseFragment<EpubSimpleListBinding>() {

    private val reader: ReaderViewModel by activityViewModels()
    private val bookmarks = BookmarkAdapter(
        click = { bookmark ->
            reader.openItemByBookmark(bookmark)
        },
        more = { bookmark,id ->
            when(id){
                R.id.menu_book_mark_delete -> reader.deleteBookmark(bookmark)
            }
        }
    )

    override fun setBinding() = EpubSimpleListBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = bookmarks

        repeatWithViewLifecycle {
            launch {
                reader.currentBookmarks.collect{
                    bookmarks.submitList(it)
                }
            }
        }

    }
}