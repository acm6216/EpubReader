package cen.xiaoyuan.epub.ui.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cen.xiaoyuan.epub.data.Chapter
import cen.xiaoyuan.epub.databinding.ItemChapterBinding
import cen.xiaoyuan.epub.utils.layoutInflater

class ChapterAdapter(
    private val click:((Chapter)->Unit)?=null
): ListAdapter<Chapter, ChapterAdapter.ChapterViewHolder>(
ChapterDiffCallback()
) {

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) = holder.bind(getItem(position),click)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder = ChapterViewHolder.from(parent)

    class ChapterViewHolder private constructor(
        val binding: ItemChapterBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(chapter: Chapter,click:((Chapter)->Unit)?=null){
            binding.chapter = chapter
            binding.root.setOnClickListener { click?.invoke(chapter) }
            binding.executePendingBindings()
        }

        companion object{
            fun from(parent: ViewGroup) = ChapterViewHolder(ItemChapterBinding.inflate(parent.context.layoutInflater,parent,false))
        }

    }

    class ChapterDiffCallback : DiffUtil.ItemCallback<Chapter>() {

        override fun areItemsTheSame(oldItem: Chapter, newItem: Chapter): Boolean {
            return (oldItem.bookUri == newItem.bookUri) && (oldItem.chapterIndex == newItem.chapterIndex)
        }

        override fun areContentsTheSame(oldItem: Chapter, newItem: Chapter): Boolean {
            return oldItem == newItem
        }
    }
}