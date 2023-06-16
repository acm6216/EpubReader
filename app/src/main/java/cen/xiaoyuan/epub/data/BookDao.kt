package cen.xiaoyuan.epub.data

import android.net.Uri
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    /* Book Table */

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooks(books:List<Book>)

    @Update
    fun updateBookLastReadingTime(book: Book)

    @Update
    fun updateBookReadStatus(book: Book)

    @Update
    fun updateBookFavorite(book: Book)

    @Transaction
    suspend fun deleteBooks(books:List<Book>) = deleteBookInfo(books)

    @Delete
    fun deleteBook(book: Book)

    suspend fun deleteBookInfo(books:List<Book>){
        books.forEach {
            deleteProgressById(it.id)
            deleteChaptersById(it.id)
            deleteBookmarksById(it.id)
            deleteBook(it)
        }
    }

    @Query("SELECT * FROM epub_books")
    fun queryBooks(): Flow<List<Book>>

    @Query("SELECT * FROM epub_books WHERE id=:uri")
    fun queryBook(uri: Uri):Book?

    /* Chapter Table */

    @Query("DELETE FROM epub_chapters WHERE id=:uri")
    fun deleteChaptersById(uri: Uri)

    @Transaction
    @Insert
    suspend fun insertChapters(chapters: List<Chapter>)

    @Query("SELECT * FROM epub_chapters WHERE bookUri=:uri")
    fun queryChapters(uri:Uri):Flow<List<Chapter>?>

    /* Progress Table */

    @Transaction
    @Update
    suspend fun updateProgress(progress: Progress)

    @Query("DELETE FROM epub_progress WHERE id=:uri")
    fun deleteProgressById(uri: Uri)

    @Transaction
    @Query("UPDATE epub_progress SET position=:position WHERE id=:uri")
    suspend fun updateProgressByUri(uri: Uri,position:Long)

    @Transaction
    @Insert
    suspend fun insertBookReaderProgress(progress: List<Progress>)

    @Query("SELECT * FROM epub_progress WHERE id=:uri")
    fun queryBookReaderProgress(uri: Uri):Flow<Progress?>

    /* Bookmark Table */

    @Query("SELECT * FROM epub_bookmarks WHERE bookUri=:uri ORDER BY date DESC")
    fun queryBookmarks(uri: Uri):Flow<List<Bookmark>?>

    @Transaction
    @Insert
    suspend fun insertBookmarks(bookmarks: List<Bookmark>)

    @Transaction
    @Delete
    fun deleteBookmarks(bookmarks: List<Bookmark>)

    @Query("DELETE FROM epub_bookmarks WHERE id=:uri")
    fun deleteBookmarksById(uri: Uri)

    /* Notes Table */

    @Transaction
    @Insert
    suspend fun insertNotes(notes:List<Note>)

    @Query("SELECT * FROM epub_notes WHERE bookUri=:uri")
    fun queryNotesByUri(uri: Uri):Flow<List<Note>?>

    @Query("SELECT * FROM epub_notes")
    fun queryNotes():Flow<List<Note>?>

    @Query("SELECT * FROm epub_notes WHERE creator=:creator")
    fun queryNotesByCreator(creator:String):Flow<List<Note>?>

    @Delete
    fun deleteNotes(notes:List<Note>)
}