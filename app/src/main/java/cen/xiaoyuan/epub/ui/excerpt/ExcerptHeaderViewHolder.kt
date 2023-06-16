package cen.xiaoyuan.epub.ui.excerpt

import android.graphics.drawable.RotateDrawable
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cen.xiaoyuan.epub.databinding.ExcerptHeaderItemBinding
import cen.xiaoyuan.epub.utils.layoutInflater

class ExcerptHeaderViewHolder private constructor(
    private val binding: ExcerptHeaderItemBinding
): RecyclerView.ViewHolder(binding.root){

    fun bind(item: ExcerptItem.ExcerptHeader,headClick:((ExcerptItem.ExcerptHeader)->Unit)?=null){
        binding.root.setOnClickListener { headClick?.invoke(item) }
        binding.text.isSelected = true
        binding.header = item
        (binding.expanded.drawable as RotateDrawable).mutate().level = if(item.expanded) 10000 else 0
        binding.executePendingBindings()
    }

    companion object{
        fun from(parent: ViewGroup) = ExcerptHeaderViewHolder(
            ExcerptHeaderItemBinding.inflate(parent.context.layoutInflater,parent,false)
        )
    }

}