package cen.xiaoyuan.epub.ui.reader

import android.view.ViewGroup
import cen.xiaoyuan.epub.databinding.ReaderListItemDividerBinding
import cen.xiaoyuan.epub.utils.layoutInflater

class DividerViewHolder private constructor(
    override val binding: ReaderListItemDividerBinding,
    readerInfo: ReaderInfo
): ReaderViewHolder<ReaderListItemDividerBinding,ReaderItem.Divider>(binding,readerInfo){

    override fun bind(item: ReaderItem.Divider,position:Int,viewModel: ReaderViewModel){
        binding.root.setOnClickListener { }
        if(!readerInfo.textColorDisable){
            binding.divider.setBackgroundColor(readerInfo.textColor)
        }
        binding.executePendingBindings()
    }

    companion object{
        fun from(parent: ViewGroup,readerInfo: ReaderInfo) = DividerViewHolder(
            ReaderListItemDividerBinding.inflate(parent.context.layoutInflater,parent,false),readerInfo
        )
    }

}