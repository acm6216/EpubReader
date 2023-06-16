package cen.xiaoyuan.epub.ui.bookinfo

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cen.xiaoyuan.epub.databinding.ItemInfoBinding
import cen.xiaoyuan.epub.utils.layoutInflater

class BookInfoAdapter: ListAdapter<BookInfo, BookInfoAdapter.BookInfoViewHolder>(
        ChapterDiffCallback()
    ) {

        override fun onBindViewHolder(holder: BookInfoViewHolder, position: Int) = holder.bind(getItem(position))

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookInfoViewHolder =
            BookInfoViewHolder.from(parent)

        class BookInfoViewHolder private constructor(
            val binding: ItemInfoBinding
        ): RecyclerView.ViewHolder(binding.root){

            fun bind(info: BookInfo){
                binding.info = info
                binding.executePendingBindings()
            }

            companion object{
                fun from(parent: ViewGroup) = BookInfoViewHolder(ItemInfoBinding.inflate(parent.context.layoutInflater,parent,false))
            }

        }

        class ChapterDiffCallback : DiffUtil.ItemCallback<BookInfo>() {

            override fun areItemsTheSame(oldItem: BookInfo, newItem: BookInfo): Boolean {
                return (oldItem.name == newItem.name) && (oldItem.description == newItem.description)
            }

            override fun areContentsTheSame(oldItem: BookInfo, newItem: BookInfo): Boolean {
                return oldItem == newItem
            }
        }
    }