package cen.xiaoyuan.epub.ui.library

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.databinding.EpubBookFilterBinding
import cen.xiaoyuan.epub.ui.BaseDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class EpubBookFilter: BaseDialog<EpubBookFilterBinding>() {

    private val library: LibraryViewModel by activityViewModels()

    override val isCreateView: Boolean
        get() = true

    override fun MaterialAlertDialogBuilder.init(): MaterialAlertDialogBuilder {
        setTitle(R.string.library_filter_title)
        setPositiveButton(R.string.close,null)
        return this
    }

    override fun scrollView(): View? = null

    override fun setLayout() = EpubBookFilterBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textInputLayout.requestFocus()
        binding.lib = library
        binding.lifecycleOwner = this.viewLifecycleOwner
        binding.executePendingBindings()
    }

}