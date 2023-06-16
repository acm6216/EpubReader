package cen.xiaoyuan.epub.ui.excerpt

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cen.xiaoyuan.epub.databinding.ExcerptContentItemBinding
import cen.xiaoyuan.epub.utils.layoutInflater

class ExcerptContentViewHolder private constructor(
    private val binding: ExcerptContentItemBinding
): RecyclerView.ViewHolder(binding.root){

    fun bind(item: ExcerptItem.ExcerptContent){
        binding.root.setOnClickListener { }
        binding.content = item
        binding.executePendingBindings()
    }

    companion object{
        fun from(parent: ViewGroup) = ExcerptContentViewHolder(
            ExcerptContentItemBinding.inflate(parent.context.layoutInflater,parent,false)
        )
    }

}