package cen.xiaoyuan.epub.ui.reader

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cen.xiaoyuan.epub.core.EpubBook
import cen.xiaoyuan.epub.core.createEpubBook
import cen.xiaoyuan.epub.data.*
import cen.xiaoyuan.epub.data.BookInfoUseCase
import cen.xiaoyuan.epub.utils.WhileViewSubscribed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val bookInfoUseCase: BookInfoUseCase
) : ViewModel() {

    private val currentUri = MutableStateFlow(Uri.EMPTY)
    val currentBook = MutableStateFlow<Book?>(null)

    suspend fun findBook(id: Uri,context: Context){
        if(id!=currentUri.value) {
            isLoading.emit(true)
            isLoaded.emit(false)
            currentUri.emit(id)
            epubBook.emit(null)
            viewModelScope.launch(Dispatchers.IO) {
                bookInfoUseCase.queryBook(id)?.let {
                    currentBook.value = (it)
                    loadEpubBook(book = it, context = context)
                }
            }
        }else {
            isLoading.emit(false)
            isLoaded.emit(true)
        }
    }

    private fun loadEpubBook(book: Book,context: Context){
        viewModelScope.launch(Dispatchers.IO) {
            context.grantUriPermission(context.packageName,book.id,Intent.FLAG_GRANT_READ_URI_PERMISSION)
            context.contentResolver.openInputStream(book.id)?.use {
                createEpubBook(it) { epub, _, _ ->
                    viewModelScope.launch(Dispatchers.Main.immediate) {
                        epubBook.emit(epub)
                    }
                }
            }
        }
    }

    val isLoading = MutableStateFlow(false)
    private val isLoaded = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentProgress = currentBook.transformLatest { book ->
        emit(null)
        book?:return@transformLatest
        bookInfoUseCase.queryBookReaderProgress(book.id).collect { progress ->
            emit(progress)
        }
    }

    val progress = combine(currentProgress, isLoading,isLoaded) { pro, loading,loaded ->
        if (pro == null) null
        else Pair(pro, loading || loaded)
    }.stateIn(viewModelScope, WhileViewSubscribed, null)

    val epubBook = MutableStateFlow<EpubBook?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentBookChapters = currentBook.transformLatest { book ->
        emit(emptyList())
        book?.also {
            bookInfoUseCase.queryChapters(it.id).collect{ chapters -> emit(chapters) }
        }
    }

    private val epubBookProgressIndex = MutableStateFlow(0)
    val epubBookChapterDetail = combine(epubBookProgressIndex,currentBookChapters){ index,chapters ->
        if(!chapters.isNullOrEmpty()) {
            try {
                val target = chapters.last { index >= it.itemIndex }
                ChapterDetail(
                    target.chapterTitle,
                    "${(index-target.itemIndex + 1F) / (target.currentChapterItem) * 100}%"
                )
            }catch (e:Exception){ ChapterDetail.EMPTY() }
        }else ChapterDetail.EMPTY()
    }.stateIn(viewModelScope, WhileViewSubscribed, ChapterDetail.EMPTY())

    val currentBookmarks = currentBook.transform { book ->
        emit(emptyList())
        book?.also {
            bookInfoUseCase.queryBookmarks(it.id).collect{ marks -> emit(marks) }
        }
    }

    val currentBookNotes = currentBook.transform { book ->
        emit(emptyList())
        book?.also {
            bookInfoUseCase.queryNotesByUri(it.id).collect{ notes -> emit(notes) }
        }
    }

    fun epubContentProgress(value:Int){
        viewModelScope.launch {
            epubContentProgress.emit(value)
            epubBookProgressIndex.emit(value)
        }
    }
    val epubContentProgress = MutableStateFlow(1000)

    val epubContent = epubBook.transform { epub ->
        emit(emptyList())
        epub ?: return@transform
        val readerItem = ArrayList<ReaderItem>()
        var index = 0
        readerItem.add(ReaderItem.Padding(chapterIndex = 0, chapterUrl = ""))
        epub.chapters.forEach{ chapter ->
            if (!chapter.isSubtitle && chapter.body.isNotEmpty()) {
                ReaderItem.Title(
                    chapterUrl = chapter.url,
                    chapterIndex = index,
                    chapterItemIndex = 0,
                    text = chapter.title
                ).also { readerItem.add(it) }
                textToItemsConverter(
                    chapter.url,
                    index, 0,
                    chapter.body
                ).also { readerItem.addAll(it) }
                ReaderItem.Divider(
                    chapterUrl = chapter.url,
                    chapterIndex = index
                ).also { readerItem.add(it) }
                index++
            }
        }
        readerItem.add(ReaderItem.End(chapterIndex = 0, chapterUrl = ""))
        emit(readerItem)
        isLoading.emit(false)
        isLoaded.emit(true)
    }.stateIn(viewModelScope, WhileViewSubscribed, emptyList())

    fun updateLastReaderIndex(index: Int) {
        val pro = progress.value ?: return
        viewModelScope.launch {
            Progress(
                id = pro.first.id,
                position = index.toLong(),
                chapterIndex = pro.first.chapterIndex
            ).also { bookInfoUseCase.updateProgress(it) }
        }
    }

    fun openItemByChapter(chapter: Chapter) {
        viewModelScope.launch {
            bookInfoUseCase.updateProgressByUri(chapter.bookUri, chapter.itemIndex)
        }
    }

    fun openItemByBookmark(bookmark: Bookmark){
        viewModelScope.launch {
            bookInfoUseCase.updateProgressByUri(bookmark.bookUri, bookmark.itemIndex.toLong())
        }
    }

    fun openItemByIndex(index: Int = -1,block:((Int)->Unit)?=null){
        val targetIndex = if(index<0) epubContent.value.size-1 else index
        block?.invoke(targetIndex)
        viewModelScope.launch {
            bookInfoUseCase.updateProgressByUri(currentUri.value,targetIndex.toLong())
        }
    }

    fun addBookmark(string: String, position:Int){
        viewModelScope.launch(Dispatchers.IO) {
            Bookmark(
                id = 0,
                bookUri = currentUri.value,
                text = string,
                description = "",
                date = System.currentTimeMillis(),
                itemIndex = position
            ).list().apply { bookInfoUseCase.insertBookmarks(this) }
        }
    }

    fun deleteBookmark(bookmark: Bookmark){
        viewModelScope.launch(Dispatchers.IO) {
            bookInfoUseCase.deleteBookmarks(bookmark.list())
        }
    }

    fun insertNote(content:String){
        viewModelScope.launch(Dispatchers.IO) {
            Note(
                id = 0L,
                bookUri = currentUri.value,
                bookTitle = currentBook.value?.title?:"",
                creator = currentBook.value?.info?.creator?:"",
                content = content,
                date = System.currentTimeMillis()
            ).list().apply { bookInfoUseCase.insertBookNotes(this) }
        }
    }

    fun deleteNotes(note: Note){
        viewModelScope.launch(Dispatchers.IO) {
            bookInfoUseCase.deleteNotes(note.list())
        }
    }

    fun exportNotes(note: Note){
        viewModelScope.launch(Dispatchers.IO) {
            bookInfoUseCase.queryNotesByUri(note.bookUri).collect{
                it?.exportList()
            }
        }
    }

    private fun List<Note>.exportList(){
        forEach {
            Log.d("TAG", "usePrint: ${it.content}")
        }
    }

    private fun Bookmark.list() = listOf(this)
    private fun Note.list() = listOf(this)
}