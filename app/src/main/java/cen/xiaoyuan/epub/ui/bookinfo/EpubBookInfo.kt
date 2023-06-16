package cen.xiaoyuan.epub.ui.bookinfo

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.activityViewModels
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.data.Book
import cen.xiaoyuan.epub.databinding.EpubBookInfoBinding
import cen.xiaoyuan.epub.ui.library.LibraryViewModel
import cen.xiaoyuan.epub.ui.BaseDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EpubBookInfo: BaseDialog<EpubBookInfoBinding>() {

    private val library: LibraryViewModel by activityViewModels()
    private val bookInfo = BookInfoAdapter()

    override fun MaterialAlertDialogBuilder.init(): MaterialAlertDialogBuilder {
        library.currentBook.value?.also {
            setTitle(getString(R.string.book_info_title,it.title))
        }
        setPositiveButton(R.string.close,null)
        return this
    }

    override val isCreateView: Boolean get() = true

    override fun scrollView(): View = binding.recyclerView

    override fun setLayout() = EpubBookInfoBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.adapter = bookInfo

        repeatWithViewLifecycle {
            launch {
                library.currentBook.collect{ book ->
                    delay(250)
                    book?.toInfoList().also { bookInfo.submitList(it) }
                }
            }
        }
    }

    private fun Book.toInfoList() = ArrayList<BookInfo>().also { list ->
        BookInfo(name = R.string.book_name, description = this.title).let { list.add(it) }
        BookInfo(name = R.string.book_chapters, description = this.chapters).let { list.add(it) }
        BookInfo(name = R.string.book_file_last_reading, description = this.lastReadingTime.stampToDate()).let { list.add(it) }
        this.info.creator.isNotEmpty { str -> BookInfo(name = R.string.book_creator, description = str).let { list.add(it) } }
        this.info.publisher.isNotEmpty { str -> BookInfo(name = R.string.book_publisher, description = str).let { list.add(it) } }
        this.info.language.isNotEmpty { str -> BookInfo(name = R.string.book_language, description = str).let { list.add(it) } }
        this.info.type.isNotEmpty { str -> BookInfo(name = R.string.book_type, description = str).let { list.add(it) } }
        this.info.identifier.isNotEmpty { str -> BookInfo(name = R.string.book_epub_id, description = str).let { list.add(it) } }
        this.info.isbn.isNotEmpty { str -> BookInfo(name = R.string.book_isbn, description = str).let { list.add(it) } }
        this.info.publicationDate.isNotEmpty { str -> BookInfo(name = R.string.book_publication_date, description = str).let { list.add(it) } }
        this.info.epubCreation.isNotEmpty { str -> BookInfo(name = R.string.book_epub_creation, description = str).let { list.add(it) } }
        this.info.description.isNotEmpty { str -> BookInfo(name = R.string.book_description, description = str).let { list.add(it) } }
        this.id.toDocumentFile().addAttrs(list)
        BookInfo(
            name = R.string.book_uri,
            description = URLDecoder.decode(this.id.toString(), "UTF-8")
        ).let { list.add(it) }
    }

    private fun DocumentFile.addAttrs(list: ArrayList<BookInfo>){
        BookInfo(name = R.string.book_file_name, description = this.name.toString()).let { list.add(it) }
        BookInfo(name = R.string.book_file_last_modified, description = this.lastModified().stampToDate()).let { list.add(it) }
    }

    private fun Long.stampToDate():String{
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA)
        val date = Date(this)
        return simpleDateFormat.format(date)
    }

    private fun Uri.toDocumentFile():DocumentFile = DocumentFile.fromSingleUri(requireContext(),this)!!

    private fun String.isNotEmpty(block:((String)->Unit)){
        if(this.isNotEmpty()) block.invoke(this)
    }

}