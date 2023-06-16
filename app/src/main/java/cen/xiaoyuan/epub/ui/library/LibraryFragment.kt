package cen.xiaoyuan.epub.ui.library

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.data.Book
import cen.xiaoyuan.epub.databinding.FragmentLibraryBinding
import cen.xiaoyuan.epub.ui.BaseFragment
import cen.xiaoyuan.epub.ui.bookinfo.EpubBookInfo
import cen.xiaoyuan.epub.ui.viewmodel.EventViewModel
import cen.xiaoyuan.epub.utils.fadeToVisibilityUnsafe
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LibraryFragment : BaseFragment<FragmentLibraryBinding>() {

    private val events: EventViewModel by activityViewModels()
    private val library: LibraryViewModel by activityViewModels()
    private val libraryAdapter = LibraryAdapter(
        click = { book ->
            if(!library.fadeToVisibility.value) {
                val directions = LibraryFragmentDirections.toReader(book.id.toString())
                findNavController().navigate(directions)
                library.updateBookLastReadingTime(book)
            }else Toast.makeText(requireContext(),R.string.toast_library_loading,Toast.LENGTH_SHORT).show()
        },
        more = { book,id ->
            when(id){
                R.id.menu_library_detail -> library.bookInfo(book){ EpubBookInfo().show(childFragmentManager,javaClass.simpleName) }
                R.id.menu_library_delete -> book.deleteAlert()
                R.id.menu_library_completed -> library.updateBookReadStatus(book,true)
                R.id.menu_library_reading -> library.updateBookReadStatus(book,false)
                R.id.menu_library_delete_all -> library.deleteAlert()
            }
        },
        favorite = { book -> library.updateBookFavorite(book) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
    }

    override fun setBinding(): FragmentLibraryBinding =
        FragmentLibraryBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        binding.recyclerView.adapter = libraryAdapter
        binding.lifecycleOwner = this.viewLifecycleOwner
        binding.lib = library
        binding.executePendingBindings()
    }

    override fun events() {
        binding.fab.setOnClickListener { events.addEpub() }
        binding.filter.setOnClickListener { showFilterDialog() }
    }

    private fun showFilterDialog() = EpubBookFilter().show(childFragmentManager,this.javaClass.simpleName)

    private fun Book.deleteAlert(){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.book_delete_title,this.title))
            .setMessage(getString(R.string.book_delete_message,this.title))
            .setPositiveButton(R.string.book_delete_pos){_,_ ->
                library.deleteBook(this)
            }
            .setNeutralButton(R.string.close,null)
            .show()
    }

    private fun LibraryViewModel.deleteAlert(){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.books_delete_title)
            .setMessage(getString(R.string.books_delete_message,this.displayBooks.value?.list?.size?:0))
            .setPositiveButton(R.string.books_delete_pos){_,_ ->
                deleteBooks()
            }
            .setNeutralButton(R.string.close,null)
            .show()
    }

    override fun launchFlow() {
        repeatWithViewLifecycle {
            launch {
                library.fadeToVisibility.collect{
                    binding.indicator.fadeToVisibilityUnsafe(it)
                }
            }
            launch {
                library.displayBooks.collect { action ->
                    action?.also {
                        libraryAdapter.submitList(it.list)
                        if(it.fromUser) binding.recyclerView.smoothScrollToPosition(0)
                    }
                }
            }
        }
    }

}