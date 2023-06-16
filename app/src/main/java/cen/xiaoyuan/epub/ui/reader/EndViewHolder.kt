package cen.xiaoyuan.epub.ui.reader

import android.view.ViewGroup
import cen.xiaoyuan.epub.databinding.ReaderListItemEndBinding
import cen.xiaoyuan.epub.utils.layoutInflater

class EndViewHolder private constructor(
    override val binding: ReaderListItemEndBinding,
    readerInfo: ReaderInfo
): ReaderViewHolder<ReaderListItemEndBinding,ReaderItem.End>(binding,readerInfo){

    override fun bind(item: ReaderItem.End,position:Int,viewModel: ReaderViewModel){
        if(!readerInfo.textColorDisable){
            binding.text.setTextColor(readerInfo.textColor)
        }
    }

    companion object{
        fun from(parent: ViewGroup,readerInfo: ReaderInfo) = EndViewHolder(
            ReaderListItemEndBinding.inflate(parent.context.layoutInflater,parent,false),readerInfo
        )
    }

}