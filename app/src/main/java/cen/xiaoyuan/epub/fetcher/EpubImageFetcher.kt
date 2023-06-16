package cen.xiaoyuan.epub.fetcher

import cen.xiaoyuan.epub.ui.reader.ImageItem
import coil.bitmap.BitmapPool
import coil.decode.DataSource
import coil.decode.Options
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.fetch.SourceResult
import coil.size.Size
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EpubImageFetcher @Inject constructor(
    private val imageSourceFactory: ImageSourceFactory
): Fetcher<ImageItem> {

    override suspend fun fetch(
        pool: BitmapPool,
        data: ImageItem,
        size: Size,
        options: Options
    ): FetchResult {
        val bufferedSource = imageSourceFactory.loadImageBufferedSource(data)
            ?: throw NullPointerException()
        return SourceResult(
            source = bufferedSource,
            mimeType = null,
            dataSource = DataSource.DISK
        )
    }

    override fun key(data: ImageItem): String = "embedded_cover_${data.image.image.path}_${data.epubBook.fileName}"

}