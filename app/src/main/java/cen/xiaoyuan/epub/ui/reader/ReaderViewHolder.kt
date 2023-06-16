package cen.xiaoyuan.epub.ui.reader

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class ReaderViewHolder<T:ViewBinding,K:ReaderItem>(
    protected open val binding:T,
    protected val readerInfo: ReaderInfo
):RecyclerView.ViewHolder(binding.root) {

    protected open var itemData:K?=null

    abstract fun bind(
        item:K,
        position:Int,
        viewModel: ReaderViewModel
    )
}