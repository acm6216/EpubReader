package cen.xiaoyuan.epub.ui.preference

import cen.xiaoyuan.epub.R

object Settings {

    val READER_TEXT_COLOR: SettingLiveData<Int> =
        IntegerSettingLiveData(
            R.string.set_reader_text_color_key, R.integer.set_reader_text_color_default
        )

    val READER_TEXT_SIZE: SettingLiveData<Int> =
        IntegerSettingLiveData(
            R.string.set_reader_text_size_key, R.integer.set_reader_text_size_default
        )

    val READER_BACKGROUND_COLOR: SettingLiveData<Int> =
        IntegerSettingLiveData(
            R.string.set_reader_background_color_key, R.integer.set_reader_background_color_default
        )

    val DARK_MODE: SettingLiveData<String> = StringSettingLiveData(
        R.string.set_dark_mode_key,R.string.set_dark_mode_default
    )

    val COLOR_FILTER: SettingLiveData<Boolean> = BooleanSettingLiveData(
        R.string.set_color_filter_key,R.bool.set_color_filter_default
    )
}