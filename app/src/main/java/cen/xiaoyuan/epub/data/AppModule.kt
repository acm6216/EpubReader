package cen.xiaoyuan.epub.data

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import cen.xiaoyuan.epub.core.EpubInfo
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    private lateinit var appDatabase: AppDatabase
    lateinit var gson: Gson

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        appDatabase = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "text_provide.db"
        ).fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {}).build()
        return appDatabase
    }

    @Provides
    fun provideTextDao(appDatabase: AppDatabase): BookDao {
        return appDatabase.bookDao()
    }

    @Provides
    @Singleton
    fun provideGson():Gson {
        gson = GsonBuilder().setPrettyPrinting().create()
        return gson
    }

}