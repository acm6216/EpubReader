package cen.xiaoyuan.epub.ui.reader

import android.annotation.SuppressLint
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.data.Progress
import cen.xiaoyuan.epub.databinding.FragmentReaderBinding
import cen.xiaoyuan.epub.ui.BaseFragment
import cen.xiaoyuan.epub.ui.interfaces.SimpleDrawerListener
import cen.xiaoyuan.epub.ui.preference.Settings
import cen.xiaoyuan.epub.ui.preference.valueCompat
import cen.xiaoyuan.epub.utils.getBoolean
import cen.xiaoyuan.epub.utils.getInt
import com.google.android.material.transition.MaterialFadeThrough
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ReaderFragment : BaseFragment<FragmentReaderBinding>() {

    override fun setBinding(): FragmentReaderBinding = FragmentReaderBinding.inflate(layoutInflater)

    private val reader: ReaderViewModel by activityViewModels()

    private val args: ReaderFragmentArgs by navArgs()

    private val observer = Observer<Any> { updatePreference() }

    private var readerAdapter: ReaderAdapter? = null

    private val textSelected:((String,Int,Int)->Unit) = { str,id,position ->
        when(id){
            R.id.menu_add_mark -> reader.addBookmark(str,position)
            R.id.menu_excerpt -> reader.insertNote(str)
        }
    }

    private val imageSelected:((ReaderItem.Image)->Unit) = {

    }

    private val closeDrawerOnBackPressed = object : OnBackPressedCallback(false) {
        @SuppressLint("RtlHardcoded")
        override fun handleOnBackPressed() {
            binding.drawerLayout.closeDrawer(Gravity.RIGHT)
            this.isEnabled = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, closeDrawerOnBackPressed)
        exitTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
        enterTransition = MaterialFadeThrough().apply {
            duration = resources.getInteger(R.integer.motion_duration_large).toLong()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this.viewLifecycleOwner
        binding.reader = reader
        binding.adapter = ReaderAdapter(
            viewModel = reader,
            readerInfo = ReaderInfo(
                textColor = R.string.set_reader_text_color_key.getInt(R.integer.set_reader_text_color_default),
                textSize = R.string.set_reader_text_size_key.getInt(R.integer.set_reader_text_size_default),
                textColorDisable = !R.string.set_color_follow_system_key.getBoolean(R.bool.set_color_follow_system_default),
                imageActionMode = imageSelected,
                textActionMode = textSelected
            )
        ).also { readerAdapter = it }
        binding.executePendingBindings()

        Settings.COLOR_FILTER.observeForever(observer)
        Settings.READER_BACKGROUND_COLOR.observeForever(observer)

        args.id?.let {
            launch { reader.findBook(Uri.parse(it),requireContext()) }
        }
        binding.drawerLayout.setup()
        binding.recyclerView.doScroll()
    }

    private fun updatePreference(){
        Settings.COLOR_FILTER.valueCompat.colorFilter()
        Settings.READER_BACKGROUND_COLOR.valueCompat.background()
    }

    private fun DrawerLayout.setup(){
        setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        addDrawerListener(object : SimpleDrawerListener() {
            @SuppressLint("RtlHardcoded")
            override fun onDrawerStateChanged(newState: Int) {
                closeDrawerOnBackPressed.isEnabled = binding.drawerLayout.isDrawerOpen(Gravity.RIGHT)
            }
        })
    }

    private fun Boolean.colorFilter(){
        requireActivity().window?.decorView?.setLayerType(View.LAYER_TYPE_HARDWARE, Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
                setSaturation(if(this@colorFilter) 0f else 1f)
            })
        })
    }

    private fun Int.background(){
        if(R.string.set_color_follow_system_key.getBoolean(R.bool.set_color_follow_system_default)) {
            binding.recyclerView.setBackgroundColor(this)
        }
    }

    @SuppressLint("RtlHardcoded")
    override fun events() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when(item.itemId){
                R.id.menu_nav_point -> binding.drawerLayout.openDrawer(Gravity.RIGHT)
                R.id.menu_nav_top -> reader.openItemByIndex(0)
                R.id.menu_nav_bottom -> reader.openItemByIndex{ it.toLong().lastReadPosition() }
            }
            true
        }
    }

    private fun RecyclerView.doScroll(){
        addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager as TopLinearLayoutManager
                reader.epubContentProgress(layoutManager.findFirstCompletelyVisibleItemPosition())
            }
        })
    }

    override fun launchFlow() {
        repeatWithViewLifecycle {
            launch {
                reader.epubContent.collect {
                    it.also {
                        readerAdapter?.submitList(it)
                    }
                }
            }
            launch {
                reader.isLoading.collect{
                    bindIndicator()
                }
            }
            launch{
                reader.epubBookChapterDetail.collect{
                    binding.chapter = it
                }
            }
            launch {
                reader.progress.collect{

                    Log.d("TAG", "onDestroyView: collect = ${it?.first?.position}")
                    it?.also { (pro,loaded) ->
                        if(loaded) pro.lastReadPosition()
                    }
                }
            }
        }
    }
    private fun bindIndicator(){
        /*binding.indicator.isIndeterminate = false
        binding.indicator.apply {
            max = 100
            progress = 50
            secondaryProgress = 20
        }*/
    }

    private val epubDisplayView get() = binding.recyclerView

    private fun storeProgress(){
        if (epubDisplayView.childCount > 0) {
            emptyTryCatch {
                reader.updateLastReaderIndex(
                    (epubDisplayView.getChildAt(0).layoutParams as RecyclerView.LayoutParams)
                        .viewAdapterPosition+1
                )
            }
        }
    }

    override fun onDestroy() {
        false.colorFilter()
        Settings.COLOR_FILTER.removeObserver(observer)
        Settings.READER_BACKGROUND_COLOR.removeObserver(observer)
        super.onDestroy()
    }

    override fun onDestroyView() {
        false.colorFilter()
        Settings.COLOR_FILTER.removeObserver(observer)
        Settings.READER_BACKGROUND_COLOR.removeObserver(observer)
        storeProgress()
        super.onDestroyView()
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("TAG", "onDestroyView: onDetach")
    }

    override fun onPause() {
        super.onPause()
        Log.d("TAG", "onDestroyView: onPause")
    }

    override fun onResume() {
        super.onResume()
        Log.d("TAG", "onDestroyView: onDetach")
    }

    private fun Progress.lastReadPosition() = this.position.lastReadPosition()

    private fun Long.lastReadPosition(){
        if(this<0) return
        launch(Dispatchers.Main.immediate) {
            delay(150)
            with(binding.recyclerView.layoutManager as TopLinearLayoutManager){
                scrollToPositionWithOffset(this@lastReadPosition.toInt(),0)
            }
        }
    }

}