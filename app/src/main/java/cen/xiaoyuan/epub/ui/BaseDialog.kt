package cen.xiaoyuan.epub.ui

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.WindowCompat
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class BaseDialog<T:ViewDataBinding>: AppCompatDialogFragment() {

    protected lateinit var binding:T
    abstract val isCreateView:Boolean
    open fun dialogCreated(){}
    abstract fun scrollView():View?
    abstract fun setLayout():T

    abstract fun MaterialAlertDialogBuilder.init(): MaterialAlertDialogBuilder

    open fun createMaterialAlertDialogBuilder():MaterialAlertDialogBuilder = MaterialAlertDialogBuilder(requireContext(),theme)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = createMaterialAlertDialogBuilder()
            .setCancelable(false)
            .setView(setLayout().also {
                binding = it
            }.root)
            .init()
            .create()
        return dialog.apply {
            WindowCompat.setDecorFitsSystemWindows(requireNotNull(window), false)
            dialogCreated()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                scrollView()?.scrollIndicators = View.SCROLL_INDICATOR_BOTTOM or View.SCROLL_INDICATOR_TOP
            }
            onCreateView(layoutInflater,binding.root as ViewGroup,savedInstanceState)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = binding.root

    protected inline fun DialogFragment.repeatWithViewLifecycle(
        minState: Lifecycle.State = Lifecycle.State.STARTED,
        crossinline block: suspend CoroutineScope.() -> Unit
    ) {
        if (minState == Lifecycle.State.INITIALIZED || minState == Lifecycle.State.DESTROYED) {
            throw IllegalArgumentException("minState must be between INITIALIZED and DESTROYED")
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(minState) {
                block()
            }
        }
    }

}