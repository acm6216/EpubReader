package cen.xiaoyuan.epub.ui.reader

import android.view.ViewGroup
import cen.xiaoyuan.epub.databinding.ReaderListItemPaddingBinding
import cen.xiaoyuan.epub.utils.layoutInflater

class PaddingViewHolder private constructor(
    override val binding: ReaderListItemPaddingBinding,
    readerInfo: ReaderInfo
): ReaderViewHolder<ReaderListItemPaddingBinding,ReaderItem.Padding>(binding,readerInfo){

    override fun bind(item: ReaderItem.Padding,position:Int,viewModel: ReaderViewModel){}

    companion object{
        fun from(parent: ViewGroup,readerInfo: ReaderInfo) = PaddingViewHolder(
            ReaderListItemPaddingBinding.inflate(parent.context.layoutInflater,parent,false),readerInfo
        )
    }

}