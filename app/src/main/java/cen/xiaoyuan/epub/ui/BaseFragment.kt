package cen.xiaoyuan.epub.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import kotlin.coroutines.CoroutineContext

abstract class BaseFragment<T: ViewDataBinding>: Fragment() ,CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.IO

    abstract fun setBinding():T
    protected lateinit var binding:T

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = setBinding().also {
        binding = it
        createViewBefore()
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchFlow()
        events()
    }

    open fun launchFlow(){}
    open fun events(){}
    open fun createViewBefore(){}

    protected fun Uri.useInputStream(block: (InputStream) -> Unit)
        = requireContext().contentResolver.openInputStream(this)?.use { block(it) }

    protected inline fun Fragment.repeatWithViewLifecycle(
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

    protected fun Fragment.emptyTryCatch(block:(()->Unit)){
        try { block() }catch (_:Exception){}
    }

    protected fun Fragment.launchTryCatch(block: () -> Unit){
        emptyTryCatch {
            launch(Dispatchers.IO) { block() }
        }
    }

    protected fun Uri.open(action:String,type:String){
        val intent = Intent(action)
        intent.setDataAndType(this, type)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        try {
            context?.startActivity(intent)
        } catch (_: ActivityNotFoundException) { }
    }

    protected inline val Double.dp: Int get() = run { toFloat().dp }
    protected inline val Int.dp: Int get() = run { toFloat().dp }
    protected inline val Float.dp: Int get() = run {
        val scale: Float = resources.displayMetrics.density
        (this * scale + 0.5f).toInt()
    }

}