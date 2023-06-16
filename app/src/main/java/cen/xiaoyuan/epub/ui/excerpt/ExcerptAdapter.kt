package cen.xiaoyuan.epub.ui.excerpt

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class ExcerptAdapter(
    private val headClick:((ExcerptItem.ExcerptHeader)->Unit)?=null
): ListAdapter<ExcerptItem, RecyclerView.ViewHolder>(
    ExcerptDiffCallback()
) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ExcerptContentViewHolder -> holder.bind(getItem(position) as ExcerptItem.ExcerptContent)
            else -> (holder as ExcerptHeaderViewHolder).bind(getItem(position) as ExcerptItem.ExcerptHeader,headClick)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when(viewType) {
            EXCERPT_HEADER -> ExcerptHeaderViewHolder.from(parent)
            else -> ExcerptContentViewHolder.from(parent)
        }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is ExcerptItem.ExcerptHeader -> EXCERPT_HEADER
        else -> EXCERPT_CONTENT
    }

    companion object{
        const val EXCERPT_HEADER = 0
        const val EXCERPT_CONTENT = 1
    }

    class ExcerptDiffCallback : DiffUtil.ItemCallback<ExcerptItem>() {

        override fun areItemsTheSame(oldItem: ExcerptItem, newItem: ExcerptItem): Boolean {
            return oldItem.type == newItem.type && oldItem.uri == newItem.uri
        }

        override fun areContentsTheSame(oldItem: ExcerptItem, newItem: ExcerptItem): Boolean {
            return oldItem == newItem
        }
    }
}