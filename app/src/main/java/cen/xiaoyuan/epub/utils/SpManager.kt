package cen.xiaoyuan.epub.utils

import android.content.SharedPreferences
import androidx.annotation.BoolRes
import androidx.annotation.IntegerRes
import androidx.annotation.StringRes
import cen.xiaoyuan.epub.ui.preference.application
import cen.xiaoyuan.epub.ui.preference.defaultSharedPreferences

private fun SharedPreferences.use(block: ((SharedPreferences.Editor) -> SharedPreferences.Editor)) {
    block(edit()).apply()
}

fun Int.getString(@StringRes strId:Int) = defaultSharedPreferences.getString(
    application.getString(this),
    application.getString(strId)
)

fun String.putInt(value: Int) = defaultSharedPreferences.use { it.putInt(this, value) }
fun Int.getInt(@IntegerRes resId:Int) = defaultSharedPreferences.getInt(
    application.getString(this),
    application.getInteger(resId)
)
fun Int.getBoolean(@BoolRes resId:Int) = defaultSharedPreferences.getBoolean(
    application.getString(this),
    application.getBoolean(resId)
)
fun String.getIntCompat(def: Int): Int {
    return try {
        defaultSharedPreferences.getInt(this, def)
    } catch (e: Exception) {
        def
    }
}
