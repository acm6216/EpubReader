package cen.xiaoyuan.epub.ui.adapters

import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.data.Note
import cen.xiaoyuan.epub.databinding.ItemBookNoteBinding
import cen.xiaoyuan.epub.utils.layoutInflater

class BookNoteAdapter(
    private val click:((Note)->Unit)?=null,
    private val more:((Note, Int)->Unit)?=null
): ListAdapter<Note, BookNoteAdapter.BookNoteViewHolder>(
ChapterDiffCallback()
) {

    override fun onBindViewHolder(holder: BookNoteViewHolder, position: Int) = holder.bind(getItem(position),click,more)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookNoteViewHolder = BookNoteViewHolder.from(parent)

    class BookNoteViewHolder private constructor(
        val binding: ItemBookNoteBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(note: Note, click:((Note)->Unit)?=null, more:((Note, Int)->Unit)?=null){
            binding.note = note
            binding.root.setOnClickListener { click?.invoke(note) }
            binding.more.setOnClickListener { view ->
                PopupMenu(view.context,view).apply {
                    inflate(R.menu.book_note_menu)
                    setOnMenuItemClickListener {
                        more?.invoke(note,it.itemId)
                        true
                    }
                    show()
                }
            }
            binding.executePendingBindings()
        }

        companion object{
            fun from(parent: ViewGroup) = BookNoteViewHolder(ItemBookNoteBinding.inflate(parent.context.layoutInflater,parent,false))
        }

    }

    class ChapterDiffCallback : DiffUtil.ItemCallback<Note>() {

        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return (oldItem.bookUri == newItem.bookUri) && (oldItem.id == newItem.id)
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}