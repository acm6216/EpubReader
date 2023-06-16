package cen.xiaoyuan.epub.ui.preference

import android.os.Bundle
import android.view.View
import androidx.preference.*
import androidx.recyclerview.widget.RecyclerView
import cen.xiaoyuan.epub.utils.applySystemWindowInsetsPadding

abstract class BasePreferenceFragment : PreferenceFragmentCompat() {

    private fun setAllPreferencesToAvoidHavingExtraSpace(preference: Preference) {
        preference.isIconSpaceReserved = false
        if (preference is PreferenceGroup)
            preference.forEach {
                setAllPreferencesToAvoidHavingExtraSpace(it)
            }
    }

    override fun setPreferenceScreen(preferenceScreen: PreferenceScreen) {
        setAllPreferencesToAvoidHavingExtraSpace(preferenceScreen)
        super.setPreferenceScreen(preferenceScreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this::class.java.superclass.superclass.getDeclaredField("mList").run {
            isAccessible = true
            (get(this@BasePreferenceFragment) as RecyclerView).apply {
                overScrollMode = View.OVER_SCROLL_NEVER
                clipToPadding = false
                applySystemWindowInsetsPadding(
                    previousApplyLeft = false,
                    previousApplyTop = false,
                    previousApplyRight = false,
                    previousApplyBottom = false,
                    previousApplyBottomFromTop = false,
                    applyLeft = false,
                    applyTop = true,
                    applyRight = false,
                    applyBottom = false,
                    applyBottomFromTop = false
                )
            }
        }
    }

    protected fun <T : Preference> Int.get(): T? = if(context!=null) findPreference(getString(this)) else null

}