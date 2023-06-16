package cen.xiaoyuan.epub

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import cen.xiaoyuan.epub.fetcher.EpubImageFetcher
import cen.xiaoyuan.epub.utils.getString
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class App:Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        setupDarkModePreference()
    }

    @Inject
    lateinit var imageFetcher: EpubImageFetcher

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .componentRegistry {
                add(imageFetcher)
            }.build()

    private fun setupDarkModePreference() {
        val darkModeSetting =R.string.set_dark_mode_key.getString(R.string.set_dark_mode_default)
        val values = resources.getStringArray(R.array.set_dark_mode_values)
        when (darkModeSetting) {
            values[0] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            values[1] -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}