package cen.xiaoyuan.epub.ui

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.databinding.DialogColorPickerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ColorPickerDialog(private val def:Int = 0, private val colors:Int = 0, private val unit:((Int)->Unit)?=null):DialogFragment() {

    private lateinit var binding: DialogColorPickerBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setView(DialogColorPickerBinding.inflate(layoutInflater).also {
                binding = it
            }.root)
            .setTitle(R.string.choose_color_title)
            .setPositiveButton(R.string.choose_color_save) { _, _ ->
                unit?.invoke(binding.lobsterPicker.getColor())
            }
            .setNegativeButton(R.string.choose_color_close, null)
            .setNeutralButton(R.string.choose_color_def){_,_ ->
                unit?.invoke(def)
            }
            .show()
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        binding.lobsterPicker.apply {
            setColorHistoryEnabled(true)
            addDecorator(binding.shadeSlider)
            setHistory(colors)
            setColor(colors)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.scrollView.scrollIndicators = View.SCROLL_INDICATOR_BOTTOM or View.SCROLL_INDICATOR_TOP
        }
    }

}