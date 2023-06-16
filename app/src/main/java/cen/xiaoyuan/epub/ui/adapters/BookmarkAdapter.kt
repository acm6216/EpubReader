package cen.xiaoyuan.epub.ui.adapters

import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.data.Bookmark
import cen.xiaoyuan.epub.databinding.ItemBookMarkBinding
import cen.xiaoyuan.epub.utils.layoutInflater

class BookmarkAdapter(
    private val click:((Bookmark)->Unit)?=null,
    private val more:((Bookmark,Int)->Unit)?=null
): ListAdapter<Bookmark, BookmarkAdapter.BookmarkViewHolder>(
ChapterDiffCallback()
) {

    override fun onBindViewHolder(holder: BookmarkViewHolder, position: Int) = holder.bind(getItem(position),click,more)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookmarkViewHolder = BookmarkViewHolder.from(parent)

    class BookmarkViewHolder private constructor(
        val binding: ItemBookMarkBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(bookmark: Bookmark, click:((Bookmark)->Unit)?=null, more:((Bookmark,Int)->Unit)?=null){
            binding.bookmark = bookmark
            binding.root.setOnClickListener { click?.invoke(bookmark) }
            binding.more.setOnClickListener { view ->
                PopupMenu(view.context,view).apply {
                    inflate(R.menu.book_mark_menu)
                    setOnMenuItemClickListener {
                        more?.invoke(bookmark,it.itemId)
                        true
                    }
                    show()
                }
            }
            binding.executePendingBindings()
        }

        companion object{
            fun from(parent: ViewGroup) = BookmarkViewHolder(ItemBookMarkBinding.inflate(parent.context.layoutInflater,parent,false))
        }

    }

    class ChapterDiffCallback : DiffUtil.ItemCallback<Bookmark>() {

        override fun areItemsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
            return (oldItem.bookUri == newItem.bookUri) && (oldItem.id == newItem.id)
        }

        override fun areContentsTheSame(oldItem: Bookmark, newItem: Bookmark): Boolean {
            return oldItem == newItem
        }
    }
}