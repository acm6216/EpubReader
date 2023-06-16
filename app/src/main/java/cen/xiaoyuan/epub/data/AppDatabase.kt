package cen.xiaoyuan.epub.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        Book::class,
        Chapter::class,
        Progress::class,
        Bookmark::class,
        Note::class],
    views = [NoteDetail::class],
    version = 11,
    exportSchema = false
)
@TypeConverters(AppDatabaseTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
}