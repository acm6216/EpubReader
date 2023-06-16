package cen.xiaoyuan.epub.ui.reader

import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.databinding.ReaderListItemTitleBinding
import cen.xiaoyuan.epub.utils.layoutInflater

class TitleViewHolder private constructor(
    override val binding: ReaderListItemTitleBinding,
    readerInfo: ReaderInfo
): ReaderViewHolder<ReaderListItemTitleBinding, ReaderItem.Title>(binding,readerInfo) {

    override fun bind(item: ReaderItem.Title,position:Int,viewModel: ReaderViewModel){
        binding.title = item
        binding.text.apply {
            textSize = readerInfo.textSize*0.28f
            setTextIsSelectable(false)
            post { setTextIsSelectable(true) }
            textActionMode = { str,id -> readerInfo.textActionMode?.invoke(str.trim { it=='\u3000'||it=='\n' },id,position)?:false }
            if(!readerInfo.textColorDisable){
                setTextColor(readerInfo.textColor)
                ResourcesCompat.getDrawable(resources, R.drawable.ic_title_logo,resources.newTheme())?.also {
                    it.setBounds(0,0,it.minimumWidth,it.minimumHeight)
                    it.setTint(readerInfo.textColor)
                    setCompoundDrawables(it,null,null,null)
                }
            }
        }
        binding.executePendingBindings()
    }

    companion object{
        fun from(parent: ViewGroup,readerInfo: ReaderInfo) = TitleViewHolder(
            ReaderListItemTitleBinding.inflate(parent.context.layoutInflater,parent,false),readerInfo
        )
    }

}