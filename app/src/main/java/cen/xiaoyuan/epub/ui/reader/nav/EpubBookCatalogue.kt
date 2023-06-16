package cen.xiaoyuan.epub.ui.reader.nav

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import cen.xiaoyuan.epub.databinding.EpubSimpleListBinding
import cen.xiaoyuan.epub.ui.BaseFragment
import cen.xiaoyuan.epub.ui.adapters.ChapterAdapter
import cen.xiaoyuan.epub.ui.reader.ReaderViewModel
import kotlinx.coroutines.launch

class EpubBookCatalogue: BaseFragment<EpubSimpleListBinding>() {

    private val reader: ReaderViewModel by activityViewModels()
    private val chapters = ChapterAdapter{ chapter -> reader.openItemByChapter(chapter) }

    override fun setBinding() = EpubSimpleListBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = chapters

        repeatWithViewLifecycle {
            launch {
                reader.currentBookChapters.collect{
                    chapters.submitList(it)
                }
            }
        }

    }
}