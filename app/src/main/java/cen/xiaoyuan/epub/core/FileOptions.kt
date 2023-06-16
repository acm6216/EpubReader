package cen.xiaoyuan.epub.core

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import cen.xiaoyuan.epub.EpubConfig
import cen.xiaoyuan.epub.ui.preference.application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

class FileOptions: CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    fun documentFiles(uri: Uri) = DocumentFile.fromTreeUri(application, uri)
        ?.listFiles()
        ?.filter {
            it.type.toString() == EpubConfig.EPUB_FILE_MIME || it.mime()== EpubConfig.EPUB_FILE_TYPE
        }?.map { it.uri }?:ArrayList()

    /**
     * 有些设备的EPUB文件的 mime 是 application/octet-stream
     * 而不是 application/epub+zip
     * 所以通过文件名后缀判别
     */
    private fun DocumentFile.mime():String{
        val tempName = name.toString().lowercase()
        return tempName.substring(tempName.indexOfLast { it=='.' }+1,tempName.length)
    }
}