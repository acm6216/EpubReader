package cen.xiaoyuan.epub.ui.library

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cen.xiaoyuan.epub.EpubConfig
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.core.EpubBook
import cen.xiaoyuan.epub.core.FileOptions
import cen.xiaoyuan.epub.core.createEpubBook
import cen.xiaoyuan.epub.data.Book
import cen.xiaoyuan.epub.data.BookInfoUseCase
import cen.xiaoyuan.epub.data.Chapter
import cen.xiaoyuan.epub.data.Progress
import cen.xiaoyuan.epub.ui.preference.application
import cen.xiaoyuan.epub.ui.reader.ReaderItem
import cen.xiaoyuan.epub.ui.reader.textToItemsConverter
import cen.xiaoyuan.epub.utils.WhileViewSubscribed
import cen.xiaoyuan.epub.utils.coverFolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val bookInfoUseCase: BookInfoUseCase
):ViewModel() {

    private val allBooks = bookInfoUseCase.queryBooks()
        .stateIn(viewModelScope, WhileViewSubscribed, emptyList())

    val bookKeywordFilter = MutableStateFlow("")
    val bookReadStatusFilter = MutableStateFlow(false)
    val bookFavoriteFilter = MutableStateFlow(false)

    private val fromUser = MutableStateFlow(false)
    private val books = combine(allBooks,bookKeywordFilter,bookReadStatusFilter,bookFavoriteFilter){ books, key,status,favorite ->
        books.filter { if(key.isNotEmpty()) it.title.contains(key)||it.info.creator.contains(key) else true }
            .filter { (if(status) it.readStatus else true) }
            .filter { (if(favorite) it.isFavorite else true) }
            .sortedByDescending { it.lastReadingTime }
    }.stateIn(viewModelScope, WhileViewSubscribed, emptyList())

    val displayBooks = combine(fromUser,books){ user,book ->
        UserAction(user,book)
    }.stateIn(viewModelScope, WhileViewSubscribed, null)

    data class UserAction(val fromUser:Boolean,val list:List<Book>)

    fun bookInfo(book: Book,block:(()->Unit)){
        viewModelScope.launch {
            currentBook.emit(book)
        }
        block.invoke()
    }
    val currentBook = MutableStateFlow<Book?>(null)

    val fadeToVisibility = MutableStateFlow(false)
    private fun fadeToVisibilityUnsafe(fade:Boolean){
        fadeToVisibility.value = fade
        fromUser.value = fade
    }

    fun updateBookLastReadingTime(book: Book){
        viewModelScope.launch(Dispatchers.IO) {
            bookInfoUseCase.updateBookLastReadingTime(
                Book(
                    id = book.id,
                    title = book.title,
                    info = book.info,
                    coverPath = book.coverPath,
                    fileName = book.fileName,
                    chapters = book.chapters,
                    lastReadingTime = System.currentTimeMillis(),
                    readStatus = book.readStatus,
                    isFavorite = book.isFavorite
                )
            )
        }
    }

    fun updateBookReadStatus(book: Book,status:Boolean){
        viewModelScope.launch(Dispatchers.IO) {
            bookInfoUseCase.updateBookReadStatus(
                Book(
                    id = book.id,
                    title = book.title,
                    info = book.info,
                    coverPath = book.coverPath,
                    fileName = book.fileName,
                    chapters = book.chapters,
                    lastReadingTime = book.lastReadingTime,
                    readStatus = status,
                    isFavorite = book.isFavorite
                )
            )
        }
    }

    fun updateBookFavorite(book: Book){
        viewModelScope.launch(Dispatchers.IO) {
            bookInfoUseCase.updateBookReadStatus(
                Book(
                    id = book.id,
                    title = book.title,
                    info = book.info,
                    coverPath = book.coverPath,
                    fileName = book.fileName,
                    chapters = book.chapters,
                    lastReadingTime = book.lastReadingTime,
                    readStatus = book.readStatus,
                    isFavorite = !book.isFavorite
                )
            )
        }
    }

    private fun insertBook(book: Book){
        viewModelScope.launch {
            bookInfoUseCase.insertBooks(listOf(book))
        }
    }

    private fun String.removeSeparator(): String = replace(File.separator, "")

    fun deleteBook(book: Book){
        viewModelScope.launch {
            bookInfoUseCase.deleteBooks(listOf(book))
        }
    }

    fun deleteBooks(){
        viewModelScope.launch {
            bookInfoUseCase.deleteBooks(books.value)
        }
    }

    private fun addBook(uri: Uri, epubBook: EpubBook, context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            bookCommit(uri, epubBook, context)
        }
    }
    private fun bookCommit(uri: Uri, epubBook: EpubBook, context: Context){
        viewModelScope.launch {
            addBookCompat(uri, epubBook, context)
        }
    }
    private suspend fun addBookCompat(uri: Uri, epubBook: EpubBook, context: Context){
        val coverPath = "${context.coverFolder.path}${File.separator}${uri.hashCode()}-"
        var itemTotal = 0L

        insertBookReaderProgress(uri)
        val chapters = ArrayList<Chapter>()

        /*val subNavPoint = epubBook.chapterNavPoints.filter { it.isSubNavPoint }.map { it.subNavPointId }
        epubBook.chapters.mapIndexed { index, epubChapter ->
            textToItemsConverter(
                chapterUrl = epubChapter.url,
                chapterPos = index,
                initialChapterItemIndex = 0,
                text = epubChapter.body
            ).let { list ->
                val sub = list.mapIndexed { index, readerItem -> Pair(index,readerItem) }
                    .filter { it.second is ReaderItem.Body && (it.second as ReaderItem.Body).navPointId in subNavPoint }
                Log.d("TAG", "addBookCompat: ${sub.map { (it.second as ReaderItem.Body).navPointId }}")
                Chapter(
                    id = 0,
                    bookUri = uri,
                    itemIndex = itemTotal,
                    chapterTitle = epubChapter.title?:"",
                    chapterIndex = index.toLong(),
                    currentChapterItem = list.size + 2L,
                    isSubtitle = false,
                    subtitleId = ""
                ).also {
                    itemTotal+=list.size + 2L
                    chapters.add(it)
                }

                sub.map {
                    Chapter(
                        id = 0,
                        bookUri = uri,
                        itemIndex = itemTotal-list.size-2L+it.first,
                        chapterTitle = (it.second as ReaderItem.Body).text,
                        chapterIndex = index.toLong(),
                        currentChapterItem = list.size + 2L,
                        isSubtitle = true,
                        subtitleId = (it.second as ReaderItem.Body).navPointId
                    )
                }.also { chapters.addAll(it) }
            }
        }*/
        epubBook.chapters.mapIndexed { index, epubChapter ->
            textToItemsConverter(
                chapterUrl = epubChapter.url,
                chapterPos = index,
                initialChapterItemIndex = 0,
                text = epubChapter.body
            ).let { list ->
                val sub = list.mapIndexed { index, readerItem -> Pair(index,readerItem) }
                    .filter { it.second is ReaderItem.Body && (it.second as ReaderItem.Body).navPointId.isNotEmpty() }

                Chapter(
                    id = 0,
                    bookUri = uri,
                    itemIndex = itemTotal,
                    chapterTitle = epubChapter.title,
                    chapterIndex = index.toLong(),
                    currentChapterItem = list.size + 2L,
                    isSubtitle = false,
                    subtitleId = ""
                ).also {
                    itemTotal+=list.size + 2L
                    chapters.add(it)
                }

                sub.map {
                    Chapter(
                        id = 0,
                        bookUri = uri,
                        itemIndex = itemTotal-list.size-2L+it.first,
                        chapterTitle = (it.second as ReaderItem.Body).text,
                        chapterIndex = index.toLong(),
                        currentChapterItem = list.size + 2L,
                        isSubtitle = true,
                        subtitleId = (it.second as ReaderItem.Body).navPointId
                    )
                }.also { chapters.addAll(it) }
            }
        }

        insertChapters(chapters)

        epubBook.images.find { epubBook.coverImagePath==it.path }?.also{
            val imgFile = File("$coverPath${it.path.removeSeparator()}")
            imgFile.parentFile?.also { parent ->
                parent.mkdirs()
                if (parent.exists())
                    imgFile.writeBytes(it.image)
            }
        }
        insertBook("$coverPath${epubBook.coverImagePath.removeSeparator()}", epubBook, uri)
    }

    private fun insertChapters(chapters:List<Chapter>){
        viewModelScope.launch {
            bookInfoUseCase.insertChapters(chapters)
        }
    }

    private fun insertBookReaderProgress(uri: Uri){
        viewModelScope.launch {
            bookInfoUseCase.insertReaderPosition(
                listOf(Progress(uri))
            )
        }
    }

    private fun insertBook(coverPath:String, epubBook: EpubBook, uri: Uri){
        insertBook(
            Book(
                id = uri,
                title = epubBook.title,
                info = epubBook.info,
                coverPath = coverPath,
                fileName = epubBook.fileName,
                chapters = epubBook.chapterNavPoints.size.toString(),
                lastReadingTime = System.currentTimeMillis(),
                readStatus = false,
                isFavorite = false
            )
        )
    }

    fun processEpubFiles(uris: List<Uri>,isFolder:Boolean = false,isPick:Boolean = false,block:(String)->Unit){
        fadeToVisibilityUnsafe(true)
        val epubFiles = if(isFolder) FileOptions().documentFiles(uris.first()) else uris
        val currentEpubBooks = books.value.map { it.id }.toSet()
        //准备写入
        val subtract = (epubFiles subtract currentEpubBooks).map {
            DocumentFile.fromSingleUri(application, it)
        }
        //已经存在
        val intersect = (epubFiles intersect currentEpubBooks).mapIndexed { index, it ->
            "${index+1}:${DocumentFile.fromSingleUri(application, it)?.name}"
        }

        val error = ArrayList<Pair<String,String>>()
        subtract.filter {
            !it?.infoProcess(isFolder,isPick){ success,name, message ->
                if(!success) error.add(Pair("${error.size+1}:${name}",message))
            }!!
        }
        fadeToVisibilityUnsafe(false)
        val errorMsg = if(error.isNotEmpty()) "\n${application.getString(R.string.add_epub_status_error)}\n${error.joinToString { "${ it.first } -> ${ it.second }\n" }.replace("\n, ","\n")}" else ""
        val intersectMsg = if(intersect.isNotEmpty()) "\n${application.getString(R.string.add_epub_status_intersect)}\n${intersect.joinToString{ "$it\n" }.replace("\n,","\n")}" else ""
        val successMsg = if(error.isEmpty() && intersect.isEmpty()) application.getString(R.string.add_epub_status_success) else ""
        block(
            application.getString(
                R.string.add_epub_message,epubFiles.size,subtract.size-error.size,
                error.size, intersect.size, "$errorMsg$intersectMsg$successMsg"
            )
        )
    }

    private fun DocumentFile.infoProcess(isFolder:Boolean = false,isPick: Boolean,block: ((Boolean,String, String) -> Unit)):Boolean{
        if(this.mime()!= EpubConfig.EPUB_FILE_TYPE) {
            block.invoke(false,name.toString(), application.getString(R.string.add_epub_error))
            return false
        }
        return uri.useInputStream { inputStream ->
            try {
                var status = false
                var eMessage = ""
                createEpubBook(inputStream) { epub, success, message ->
                    if (success) {
                        uri.takePersistableUriPermission(isFolder||isPick)
                        epub?.addDatabase(uri)
                    }
                    eMessage = message
                    status = success
                }
                block(status,name.toString(), eMessage)
                status
            }catch (e:java.lang.Exception){
                block(false,name.toString(),e.message.toString())
                false
            }
        }!!
    }

    private fun EpubBook.addDatabase(uri: Uri) = addBook(uri, this, application)

    private fun Uri.takePersistableUriPermission(isFolder:Boolean = false): Uri {
        if(!isFolder) application.contentResolver.takePersistableUriPermission(
            this, Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        else application.grantUriPermission(
            application.packageName,this, Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        return this
    }

    private fun Uri.useInputStream(block: (InputStream) -> Boolean) =
        application.contentResolver.openInputStream(this)?.use { block(it) }

    private fun DocumentFile.mime():String{
        val tempName = name.toString().lowercase()
        return tempName.substring(tempName.indexOfLast { it=='.' }+1,tempName.length)
    }

}