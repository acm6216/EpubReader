package cen.xiaoyuan.epub.ui.reader

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ReaderAdapter(
    private val viewModel: ReaderViewModel,
    private val readerInfo: ReaderInfo
): ListAdapter<ReaderItem,RecyclerView.ViewHolder>(
    ReaderDiffCallback()
) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int){
        when(holder){
            is BodyViewHolder -> holder.bind(getItem(position) as ReaderItem.Body,position, viewModel)
            is ImageViewHolder -> holder.bind(getItem(position) as ReaderItem.Image,position,viewModel)
            is DividerViewHolder -> holder.bind(getItem(position) as ReaderItem.Divider,position,viewModel)
            is TitleViewHolder -> holder.bind(getItem(position) as ReaderItem.Title,position,viewModel)
            is PaddingViewHolder -> holder.bind(getItem(position) as ReaderItem.Padding,position,viewModel)
            is EndViewHolder -> holder.bind(getItem(position) as ReaderItem.End,position,viewModel)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when(viewType){
            READER_ITEM_BODY -> BodyViewHolder.from(parent,readerInfo)
            READER_ITEM_IMAGE -> ImageViewHolder.from(parent,readerInfo)
            READER_ITEM_DIVIDER -> DividerViewHolder.from(parent,readerInfo)
            READER_ITEM_PADDING -> PaddingViewHolder.from(parent,readerInfo)
            READER_ITEM_END -> EndViewHolder.from(parent,readerInfo)
            else -> TitleViewHolder.from(parent,readerInfo)
        }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is ReaderItem.Body -> READER_ITEM_BODY
        is ReaderItem.Image -> READER_ITEM_IMAGE
        is ReaderItem.Divider -> READER_ITEM_DIVIDER
        is ReaderItem.Padding -> READER_ITEM_PADDING
        is ReaderItem.End -> READER_ITEM_END
        else -> READER_ITEM_TITLE
    }

    class ReaderDiffCallback : DiffUtil.ItemCallback<ReaderItem>() {

        override fun areItemsTheSame(oldItem: ReaderItem, newItem: ReaderItem): Boolean {
            return oldItem.chapterUrl == newItem.chapterUrl
        }

        override fun areContentsTheSame(oldItem: ReaderItem, newItem: ReaderItem): Boolean {
            return oldItem == newItem
        }
    }

    companion object{
        const val READER_ITEM_BODY = 0
        const val READER_ITEM_IMAGE = 1
        const val READER_ITEM_DIVIDER = 2
        const val READER_ITEM_TITLE = 3
        const val READER_ITEM_PADDING = 4
        const val READER_ITEM_END= 5
    }
}