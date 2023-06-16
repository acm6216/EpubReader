package cen.xiaoyuan.epub.ui.reader

import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.databinding.ReaderListItemImageBinding
import cen.xiaoyuan.epub.utils.layoutInflater

class ImageViewHolder private constructor(
    override val binding: ReaderListItemImageBinding,
    readerInfo: ReaderInfo
): ReaderViewHolder<ReaderListItemImageBinding,ReaderItem.Image>(binding,readerInfo){

    private val popupMenu = PopupMenu(binding.root.context,binding.pic).apply {
        menuInflater.inflate(R.menu.reader_image,menu)
        setOnMenuItemClickListener {
            itemData?.let { image -> readerInfo.imageActionMode?.invoke(image) }
            true
        }
    }

    override fun bind(item: ReaderItem.Image,position:Int,viewModel: ReaderViewModel){
        itemData = item
        binding.root.setOnClickListener {
            popupMenu.show()
        }
        viewModel.epubBook.value?.also {
            binding.image = ImageItem(it,item)
        }
        binding.executePendingBindings()
    }

    companion object{
        fun from(parent: ViewGroup,readerInfo: ReaderInfo) = ImageViewHolder(
            ReaderListItemImageBinding.inflate(parent.context.layoutInflater,parent,false),readerInfo
        )
    }

}