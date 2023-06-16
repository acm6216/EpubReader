package cen.xiaoyuan.epub.ui.library

import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cen.xiaoyuan.epub.R
import cen.xiaoyuan.epub.data.Book
import cen.xiaoyuan.epub.databinding.ItemLibraryBinding
import cen.xiaoyuan.epub.ui.views.IconStateButton
import cen.xiaoyuan.epub.utils.layoutInflater

class LibraryAdapter(
    private val click:((Book)->Unit)?=null,
    private val more:((Book,Int)->Unit)?=null,
    private val favorite:((Book)->Unit)?=null
): ListAdapter<Book, LibraryAdapter.LibraryViewHolder>(
    LibraryDiffCallback()
) {

    override fun onBindViewHolder(holder: LibraryViewHolder, position: Int) = holder.bind(getItem(position),click,more,favorite)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LibraryViewHolder.from(parent)

    class LibraryViewHolder private constructor(
        val binding:ItemLibraryBinding
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(book: Book,click:((Book)->Unit)?=null,more:((Book,Int)->Unit)?=null,favorite:((Book)->Unit)?=null){
            binding.book = book
            binding.click.setOnClickListener { click?.invoke(book) }
            binding.title.isSelected = true
            binding.creator.isSelected = true
            binding.favorite.apply {
                isActivated = book.isFavorite
                setOnClickListener {
                    (it as IconStateButton).isActivated = !book.isFavorite
                    favorite?.invoke(book)
                }
            }
            binding.more.setOnClickListener {
                PopupMenu(it.context,it).apply {
                    inflate(R.menu.library_menu)
                    menu.findItem(R.id.menu_library_reading).isVisible = book.readStatus
                    menu.findItem(R.id.menu_library_completed).isVisible = !book.readStatus
                    setOnMenuItemClickListener { item ->
                        more?.invoke(book,item.itemId)
                        true
                    }
                    show()
                }
            }
            binding.executePendingBindings()
        }

        companion object{
            fun from(parent: ViewGroup) = LibraryViewHolder(ItemLibraryBinding.inflate(parent.context.layoutInflater,parent,false))
        }

    }

    class LibraryDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }
}