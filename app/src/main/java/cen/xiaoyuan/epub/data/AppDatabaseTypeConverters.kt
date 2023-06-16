package cen.xiaoyuan.epub.data

import android.net.Uri
import androidx.room.TypeConverter
import cen.xiaoyuan.epub.core.EpubInfo
import cen.xiaoyuan.epub.data.AppModule.provideGson

class AppDatabaseTypeConverters {

    @TypeConverter
    fun uriToString(value: Uri): String = value.toString()

    @TypeConverter
    fun stringToUri(value:String):Uri = Uri.parse(value)

    @TypeConverter
    fun epubIndoToString(value: EpubInfo):String  {
        return provideGson().toJson(value)
    }

    @TypeConverter
    fun stringToEpubInfo(value: String):EpubInfo{
        return provideGson().fromJson(value,EpubInfo::class.java)
    }
}