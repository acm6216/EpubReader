package cen.xiaoyuan.epub.fetcher

import cen.xiaoyuan.epub.ui.reader.ImageItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.BufferedSource
import okio.buffer
import okio.source
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageSourceFactory @Inject constructor(){

    private suspend fun loadImage(imageItem: ImageItem):ByteArray? = withContext(Dispatchers.IO) {
        val tag = imageItem.image.image.path
        imageItem.epubBook.images.find { it.path==tag }?.image
    }

    suspend fun loadImageBufferedSource(imageItem: ImageItem): BufferedSource?{
        val bytes = loadImage(imageItem) ?: return null
        val source = ByteArrayInputStream(bytes).source()
        source.timeout().timeout(50, TimeUnit.MILLISECONDS)
        return source.buffer()
    }
}