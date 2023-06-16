package cen.xiaoyuan.epub.ui.preference

import android.content.SharedPreferences
import androidx.annotation.BoolRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager
import cen.xiaoyuan.epub.utils.getBoolean
import cen.xiaoyuan.epub.utils.getInteger

@Suppress("UNCHECKED_CAST")
val <T> LiveData<T>.valueCompat: T
    get() = value as T

val defaultSharedPreferences: SharedPreferences by lazy {
    PreferenceManager.getDefaultSharedPreferences(application)
}

class StringSettingLiveData(
    nameSuffix: String?,
    @StringRes keyRes: Int,
    keySuffix: String?,
    @StringRes defaultValueRes: Int
) : SettingLiveData<String>(nameSuffix, keyRes, keySuffix, defaultValueRes) {
    constructor(@StringRes keyRes: Int, @StringRes defaultValueRes: Int) : this(
        null, keyRes, null, defaultValueRes
    )

    init {
        init()
    }

    override fun getDefaultValue(@StringRes defaultValueRes: Int): String =
        application.getString(defaultValueRes)

    override fun getValue(
        sharedPreferences: SharedPreferences,
        key: String,
        defaultValue: String
    ): String = sharedPreferences.getString(key, defaultValue)!!

    override fun putValue(sharedPreferences: SharedPreferences, key: String, value: String) {
        sharedPreferences.edit { putString(key, value) }
    }
}

class BooleanSettingLiveData(
    nameSuffix: String?,
    @StringRes keyRes: Int,
    keySuffix: String?,
    @BoolRes defaultValueRes: Int
) : SettingLiveData<Boolean>(nameSuffix, keyRes, keySuffix, defaultValueRes) {
    constructor(@StringRes keyRes: Int, @BoolRes defaultValueRes: Int) : this(
        null, keyRes, null, defaultValueRes
    )

    init {
        init()
    }

    override fun getDefaultValue(@BoolRes defaultValueRes: Int): Boolean =
        application.getBoolean(defaultValueRes)

    override fun getValue(
        sharedPreferences: SharedPreferences,
        key: String,
        defaultValue: Boolean
    ): Boolean = sharedPreferences.getBoolean(key, defaultValue)

    override fun putValue(sharedPreferences: SharedPreferences, key: String, value: Boolean) {
        sharedPreferences.edit { putBoolean(key, value) }
    }
}

class IntegerSettingLiveData(
    nameSuffix: String?,
    @StringRes keyRes: Int,
    keySuffix: String?,
    @IntegerRes defaultValueRes: Int
) : SettingLiveData<Int>(nameSuffix, keyRes, keySuffix, defaultValueRes) {
    constructor(@StringRes keyRes: Int, @IntegerRes defaultValueRes: Int) : this(
        null, keyRes, null, defaultValueRes
    )

    init {
        init()
    }

    override fun getDefaultValue(@IntegerRes defaultValueRes: Int): Int =
        application.getInteger(defaultValueRes)

    override fun getValue(
        sharedPreferences: SharedPreferences,
        key: String,
        defaultValue: Int
    ): Int = sharedPreferences.getInt(key, defaultValue)

    override fun putValue(sharedPreferences: SharedPreferences, key: String, value: Int) {
        sharedPreferences.edit { putInt(key, value) }
    }
}
