package cen.xiaoyuan.epub.ui.preference

import android.os.Bundle
import androidx.preference.Preference
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.ui.ColorPickerDialog
import cen.xiaoyuan.epub.utils.getIntCompat
import cen.xiaoyuan.epub.utils.putInt

class SettingsFragment : BasePreferenceFragment() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
        R.string.set_reader_text_color_key.chooseColor(0xff000000.toInt())
        R.string.set_reader_background_color_key.chooseColor(0xffffffff.toInt())
    }

    private fun Int.chooseColor(def:Int){
        get<ColorPreference>()?.apply {
            initView { setColorValue(def) }
            onPreferenceClickListener = Preference.OnPreferenceClickListener { it as ColorPreference
                showColorDialog(it,def){ color ->
                    it.setColorValue(color,true)
                }
                true
            }
        }
    }

    private fun showColorDialog(p:ColorPreference,def:Int,unit:(Int)->Unit){
        ColorPickerDialog(def,p.key.getIntCompat(def)){
            p.key.putInt(it)
            unit.invoke(it)
        }.show(childFragmentManager,javaClass.simpleName)
    }

}