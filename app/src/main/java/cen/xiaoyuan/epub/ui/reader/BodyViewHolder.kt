package cen.xiaoyuan.epub.ui.reader

import android.view.ViewGroup
import cen.xiaoyuan.epub.databinding.ReaderListItemBodyBinding
import cen.xiaoyuan.epub.utils.layoutInflater

class BodyViewHolder private constructor(
    override val binding: ReaderListItemBodyBinding,
    readerInfo: ReaderInfo
): ReaderViewHolder<ReaderListItemBodyBinding,ReaderItem.Body>(binding,readerInfo){

    override fun bind(item: ReaderItem.Body,position:Int,viewModel: ReaderViewModel){
        binding.body = item
        binding.text.apply {
            textSize = readerInfo.textSize*0.26f
            textActionMode = { str,id -> readerInfo.textActionMode?.invoke(str.trim { it=='\u3000'||it=='\n' },id,position)?:false }
            if(!readerInfo.textColorDisable) {
                setTextColor(readerInfo.textColor)
                setLinkTextColor(readerInfo.textColor)
            }
        }
        binding.executePendingBindings()
        binding.text.apply {
            setTextIsSelectable(false)
            run { setTextIsSelectable(true) }
        }
    }

    companion object{
        fun from(parent: ViewGroup,readerInfo: ReaderInfo) = BodyViewHolder(
            ReaderListItemBodyBinding.inflate(parent.context.layoutInflater,parent,false),readerInfo
        )
    }

}