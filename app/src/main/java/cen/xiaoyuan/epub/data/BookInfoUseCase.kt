package cen.xiaoyuan.epub.data

import android.net.Uri
import cen.xiaoyuan.epub.data.*
import javax.inject.Inject

class BookInfoUseCase @Inject constructor(private val bookDao: BookDao) {

    suspend fun deleteBooks(books:List<Book>) = bookDao.deleteBooks(books)
    suspend fun insertBooks(books:List<Book>) = bookDao.insertBooks(books)
    fun updateBookLastReadingTime(book: Book) = bookDao.updateBookLastReadingTime(book)
    fun updateBookReadStatus(book: Book) = bookDao.updateBookReadStatus(book)
    fun queryBook(id: Uri) = bookDao.queryBook(id)
    fun queryBooks() = bookDao.queryBooks()

    suspend fun updateProgress(progress: Progress) = bookDao.updateProgress(progress)
    suspend fun updateProgressByUri(uri: Uri,position:Long) = bookDao.updateProgressByUri(uri,position)
    suspend fun insertReaderPosition(progress: List<Progress>) = bookDao.insertBookReaderProgress(progress)
    fun queryBookReaderProgress(uri: Uri) = bookDao.queryBookReaderProgress(uri)

    suspend fun insertChapters(chapters: List<Chapter>) = bookDao.insertChapters(chapters)
    fun queryChapters(uri: Uri) = bookDao.queryChapters(uri)

    suspend fun insertBookmarks(bookmarks: List<Bookmark>) = bookDao.insertBookmarks(bookmarks)
    fun queryBookmarks(uri: Uri) = bookDao.queryBookmarks(uri)
    fun deleteBookmarks(bookmarks: List<Bookmark>) = bookDao.deleteBookmarks(bookmarks)

    suspend fun insertBookNotes(notes:List<Note>) = bookDao.insertNotes(notes)
    fun queryNotesByUri(uri: Uri) = bookDao.queryNotesByUri(uri)
    fun queryNotesByCreator(creator:String) = bookDao.queryNotesByCreator(creator)
    fun queryNotes() = bookDao.queryNotes()
    fun deleteNotes(notes:List<Note>) = bookDao.deleteNotes(notes)

}