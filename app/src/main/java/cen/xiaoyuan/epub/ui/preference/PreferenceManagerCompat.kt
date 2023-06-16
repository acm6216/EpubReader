package cen.xiaoyuan.epub.ui.preference

import android.content.Context

object PreferenceManagerCompat {
    fun getDefaultSharedPreferencesName(context: Context): String =
        "${context.packageName}_preferences"

    val defaultSharedPreferencesMode: Int
        get() = Context.MODE_PRIVATE
}