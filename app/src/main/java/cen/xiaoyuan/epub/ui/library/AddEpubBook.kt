package cen.xiaoyuan.epub.ui.library

import android.app.Dialog
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import cen.xiaoyuan.epub.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddEpubBook(private val files:(()->Unit)?=null, private val folder:(()->Unit)?=null):DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = MaterialAlertDialogBuilder(requireContext(),theme)
            .setCancelable(false)
            .setTitle(R.string.add_epub_book_title)
            .setMessage(R.string.add_epub_book_message)
            .setNegativeButton(R.string.add_epub_files){ _,_ -> files?.invoke() }
            .setNeutralButton(R.string.add_epub_folder){ _,_ -> folder?.invoke() }
            .setPositiveButton(R.string.add_epub_close,null)
            .create()
        return dialog.apply {
            WindowCompat.setDecorFitsSystemWindows(requireNotNull(window), false)
        }
    }
}